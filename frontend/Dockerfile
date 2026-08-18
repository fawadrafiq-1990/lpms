###############################################################################
# Stage 1: Build the React application
###############################################################################
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files first for layer caching
COPY package.json package-lock.json ./

# Deterministic install (fail if lockfile is out of sync)
RUN --mount=type=cache,target=/root/.npm \
    npm ci --ignore-scripts && \
    # Verify install succeeded
    npm ls --depth=0 2>/dev/null || true

# Copy source and build
COPY . .

ARG VITE_API_URL
ENV VITE_API_URL=${VITE_API_URL}

RUN npm run build && \
    # Verify build output exists
    test -d dist

###############################################################################
# Stage 2: Pre-compress assets (gzip + brotli)
###############################################################################
FROM node:20-alpine AS compressor

WORKDIR /app

# Copy built assets
COPY --from=builder /app/dist ./dist

# Install compression tools
RUN apk add --no-cache gzip brotli

# Compress all static assets
RUN find dist -type f \
    \( -iname '*.js' -o -iname '*.css' -o -iname '*.html' -o -iname '*.json' -o -iname '*.svg' -o -iname '*.woff' -o -iname '*.woff2' -o -iname '*.ttf' -o -iname '*.eot' \) \
    -exec gzip -9 -k {} \; \
    -exec brotli -9 -k {} \;

###############################################################################
# Stage 3: Production nginx serving
###############################################################################
FROM nginx:1.27-alpine AS runtime

# Labels
LABEL org.opencontainers.image.title="LPMS Frontend" \
      org.opencontainers.image.description="Legal Practice Management System - React Frontend" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.vendor="LPMS" \
      maintainer="lpms-team"

# Remove default nginx config and content
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# Copy compressed assets from compressor stage
COPY --from=compressor /app/dist /usr/share/nginx/html

# Copy production nginx configuration
COPY nginx.prod.conf /etc/nginx/conf.d/default.conf

# Optimize nginx worker processes (match CPU cores)
ENV NGINX_WORKER_PROCESSES=auto

# Create cache and temp directories with proper ownership
RUN mkdir -p /var/cache/nginx /var/log/nginx /tmp/nginx && \
    chown -R nginx:nginx /var/cache/nginx /var/log/nginx /tmp/nginx /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 --start-interval=5s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Drop root privileges where possible; nginx master must be root for bind
# nginx worker processes run as the 'nginx' user (configured in nginx.conf)
CMD ["nginx", "-g", "daemon off;"]
