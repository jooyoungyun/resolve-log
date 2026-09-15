package com.resolvelog;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiIntegrationTest {
  @Value("${local.server.port}")
  int port;

  @Autowired ObjectMapper json;
  @Autowired JdbcTemplate jdbc;
  final HttpClient client = HttpClient.newHttpClient();
  final LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
  String token;
  String email;

  @BeforeEach
  void account() throws Exception {
    email = "test-" + UUID.randomUUID() + "@example.com";
    var r =
        call(
            "POST",
            "/api/auth/register",
            null,
            Map.of("email", email, "displayName", "테스트 기록자", "password", "ResolveTest123!"));
    assertEquals(201, r.statusCode(), r.body());
    token = (String) body(r).get("accessToken");
  }

  HttpResponse<String> call(String method, String path, String bearer, Object payload)
      throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .timeout(Duration.ofSeconds(20));
    if (bearer != null) builder.header("Authorization", "Bearer " + bearer);
    if (payload != null) builder.header("Content-Type", "application/json");
    return client.send(
        builder
            .method(
                method,
                payload == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(payload)))
            .build(),
        HttpResponse.BodyHandlers.ofString());
  }

  @SuppressWarnings("unchecked")
  Map<String, Object> body(HttpResponse<String> r) throws Exception {
    return json.readValue(r.body(), Map.class);
  }

  Map<String, Object> input() {
    var r = new LinkedHashMap<String, Object>();
    r.put("title", "하루 30분 산책");
    r.put("reason", "꾸준한 실천을 위해");
    r.put("category", "HEALTH");
    r.put("startDate", today.minusDays(7).toString());
    r.put("endDate", null);
    r.put("weekdays", List.of(1, 2, 3, 4, 5, 6, 7));
    r.put("archived", false);
    return r;
  }

  Map<String, Object> goal() throws Exception {
    var r = call("POST", "/api/goals", token, input());
    assertEquals(201, r.statusCode(), r.body());
    return body(r);
  }

  String entryPath(Object goalId, LocalDate date) {
    return "/api/goals/" + goalId + "/entries/" + date;
  }

  Map<String, Object> entry(String status) {
    return Map.of("status", status, "note", "오늘도 한 걸음 걸었습니다.", "minutes", 30);
  }

  @Test
  void registrationAndLoginValidation() throws Exception {
    assertEquals(
        409,
        call(
                "POST",
                "/api/auth/register",
                null,
                Map.of(
                    "email",
                    email.toUpperCase(Locale.ROOT),
                    "displayName",
                    "같은 계정",
                    "password",
                    "ResolveTest123!"))
            .statusCode());
    assertEquals(
        401,
        call("POST", "/api/auth/login", null, Map.of("email", email, "password", "incorrect"))
            .statusCode());
    assertEquals(
        200,
        call("POST", "/api/auth/login", null, Map.of("email", email, "password", "ResolveTest123!"))
            .statusCode());
    assertEquals(
        400,
        call(
                "POST",
                "/api/auth/register",
                null,
                Map.of("email", "invalid", "displayName", "이름", "password", "short"))
            .statusCode());
  }

  @Test
  void authenticationAndOwnership() throws Exception {
    var g = goal();
    assertEquals(401, call("GET", "/api/goals", null, null).statusCode());
    assertEquals(401, call("GET", "/api/goals", "invalid-token", null).statusCode());
    var other =
        body(
            call(
                "POST",
                "/api/auth/register",
                null,
                Map.of(
                    "email",
                    UUID.randomUUID() + "@example.com",
                    "displayName",
                    "다른 회원",
                    "password",
                    "ResolveTest123!")));
    var otherToken = (String) other.get("accessToken");
    assertEquals(404, call("GET", "/api/goals/" + g.get("id"), otherToken, null).statusCode());
    assertEquals(
        404, call("PUT", entryPath(g.get("id"), today), otherToken, entry("DONE")).statusCode());
    assertEquals(404, call("DELETE", "/api/goals/" + g.get("id"), otherToken, null).statusCode());
    assertEquals("[]", call("GET", "/api/goals", otherToken, null).body());
  }

  @Test
  void invalidGoalDatesAndWeekdays() throws Exception {
    var data = input();
    data.put("weekdays", List.of());
    assertEquals(400, call("POST", "/api/goals", token, data).statusCode());
    data.put("weekdays", List.of(1, 1));
    assertEquals(400, call("POST", "/api/goals", token, data).statusCode());
    data.put("weekdays", List.of(8));
    assertEquals(400, call("POST", "/api/goals", token, data).statusCode());
    data.put("weekdays", List.of(1));
    data.put("endDate", today.minusDays(8).toString());
    assertEquals(400, call("POST", "/api/goals", token, data).statusCode());
  }

  @Test
  void entryUpsertAndDashboard() throws Exception {
    var g = goal();
    var path = entryPath(g.get("id"), today);
    var first = call("PUT", path, token, entry("PARTIAL"));
    var second = call("PUT", path, token, entry("DONE"));
    assertEquals(200, first.statusCode(), first.body());
    assertEquals(200, second.statusCode(), second.body());
    assertEquals(body(first).get("id"), body(second).get("id"));
    assertEquals(
        1L,
        jdbc.queryForObject(
            "select count(*) from journal_entry where goal_id=?",
            Long.class,
            UUID.fromString((String) g.get("id"))));
    var dash = body(call("GET", "/api/dashboard?date=" + today, token, null));
    assertEquals(1, ((Number) dash.get("doneCount")).intValue());
    assertEquals(100, ((Number) dash.get("completionRate")).intValue());
    assertEquals(30, ((Number) dash.get("minutes")).intValue());
    var list =
        body(
            call(
                "GET",
                "/api/entries?from=" + today + "&to=" + today + "&status=DONE",
                token,
                null));
    assertEquals(1, ((Number) list.get("totalElements")).intValue());
  }

  @Test
  void futureAndUnscheduledEntriesAreRejected() throws Exception {
    var g = goal();
    assertEquals(
        400,
        call("PUT", entryPath(g.get("id"), today.plusDays(1)), token, entry("DONE")).statusCode());
    assertEquals(
        400,
        call(
                "PUT",
                entryPath(g.get("id"), today),
                token,
                Map.of("status", "DONE", "note", "", "minutes", 1441))
            .statusCode());
    var data = input();
    data.put("weekdays", List.of(today.getDayOfWeek().getValue() % 7 + 1));
    var r = body(call("POST", "/api/goals", token, data));
    assertEquals(
        400, call("PUT", entryPath(r.get("id"), today), token, entry("DONE")).statusCode());
  }

  @Test
  void archiveKeepsEntriesAndPreventsNewRecords() throws Exception {
    var g = goal();
    assertEquals(
        200,
        call("PUT", entryPath(g.get("id"), today.minusDays(1)), token, entry("DONE")).statusCode());
    var update = input();
    update.put("archived", true);
    update.put("version", g.get("version"));
    assertEquals(200, call("PUT", "/api/goals/" + g.get("id"), token, update).statusCode());
    assertEquals(
        400, call("PUT", entryPath(g.get("id"), today), token, entry("DONE")).statusCode());
    assertEquals(
        200,
        call("PUT", entryPath(g.get("id"), today.minusDays(1)), token, entry("PARTIAL"))
            .statusCode());
    assertEquals(
        1L,
        jdbc.queryForObject(
            "select count(*) from journal_entry where goal_id=?",
            Long.class,
            UUID.fromString((String) g.get("id"))));
  }

  @Test
  void concurrentEntriesProduceOneRow() throws Exception {
    var g = goal();
    String path = entryPath(g.get("id"), today);
    try (var executor = Executors.newFixedThreadPool(4)) {
      var futures = new ArrayList<Future<Integer>>();
      for (int i = 0; i < 4; i++)
        futures.add(executor.submit(() -> call("PUT", path, token, entry("DONE")).statusCode()));
      for (var f : futures) assertEquals(200, f.get());
    }
    assertEquals(
        1L,
        jdbc.queryForObject(
            "select count(*) from journal_entry where goal_id=?",
            Long.class,
            UUID.fromString((String) g.get("id"))));
  }

  @Test
  void staleGoalAndPeriodChangeAreRejected() throws Exception {
    var g = goal();
    call("PUT", entryPath(g.get("id"), today.minusDays(1)), token, entry("DONE"));
    var update = input();
    update.put("version", g.get("version"));
    update.put("startDate", today.toString());
    assertEquals(400, call("PUT", "/api/goals/" + g.get("id"), token, update).statusCode());
    update.put("startDate", today.minusDays(7).toString());
    update.put("title", "새 이름");
    assertEquals(200, call("PUT", "/api/goals/" + g.get("id"), token, update).statusCode());
    assertEquals(409, call("PUT", "/api/goals/" + g.get("id"), token, update).statusCode());
  }

  @Test
  void deleteCascadesAndEntryDeleteIsIdempotent() throws Exception {
    var g = goal();
    String path = entryPath(g.get("id"), today);
    call("PUT", path, token, entry("DONE"));
    assertEquals(204, call("DELETE", path, token, null).statusCode());
    assertEquals(204, call("DELETE", path, token, null).statusCode());
    call("PUT", path, token, entry("DONE"));
    assertEquals(204, call("DELETE", "/api/goals/" + g.get("id"), token, null).statusCode());
    assertEquals(
        0L,
        jdbc.queryForObject(
            "select count(*) from journal_entry where goal_id=?",
            Long.class,
            UUID.fromString((String) g.get("id"))));
    assertEquals(
        0L,
        jdbc.queryForObject(
            "select count(*) from goal_weekday where goal_id=?",
            Long.class,
            UUID.fromString((String) g.get("id"))));
  }

  @Test
  void calendarAndPaginationBoundaries() throws Exception {
    var g = goal();
    call("PUT", entryPath(g.get("id"), today), token, entry("DONE"));
    var month = YearMonth.from(today);
    var cal = body(call("GET", "/api/calendar?month=" + month, token, null));
    assertEquals(month.lengthOfMonth(), ((List<?>) cal.get("days")).size());
    assertEquals(
        400,
        call("GET", "/api/entries?from=" + today + "&to=" + today + "&page=0", token, null)
            .statusCode());
    assertEquals(
        400,
        call("GET", "/api/entries?from=" + today + "&to=" + today.minusDays(1), token, null)
            .statusCode());
    assertEquals(400, call("GET", "/api/calendar?month=invalid", token, null).statusCode());
    assertEquals(200, call("GET", "/actuator/health", null, null).statusCode());
  }
}
