FROM node:24-bookworm-slim AS build
WORKDIR /build

# 사내 인증서를 Node.js 빌드 환경에 등록
COPY cert/forcepoint-f1e-np-session.cer /tmp/company-certs/forcepoint.cer
COPY cert/somansa-root-ca.cer /tmp/company-certs/somansa.cer

RUN node -e "const fs = require('node:fs'); \
    const { X509Certificate } = require('node:crypto'); \
    const files = ['/tmp/company-certs/forcepoint.cer', '/tmp/company-certs/somansa.cer']; \
    const pem = files.map(f => new X509Certificate(fs.readFileSync(f)).toString()).join('\n'); \
    fs.writeFileSync('/tmp/company-ca.pem', pem);"

ENV NODE_EXTRA_CA_CERTS=/tmp/company-ca.pem

COPY package*.json ./
# Nuxt prepare needs the project config; dependency installation stays cacheable.
RUN npm ci --ignore-scripts --no-audit --no-fund
COPY . .
RUN npx nuxt prepare && npm run typecheck && npm run build

FROM node:24-bookworm-slim
WORKDIR /app
ENV NODE_ENV=production NITRO_HOST=0.0.0.0 NITRO_PORT=3000
COPY --from=build --chown=node:node /build/.output ./.output
USER node
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]

