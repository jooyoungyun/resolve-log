FROM node:24-bookworm-slim AS build
WORKDIR /build
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

