# SECURITY.md - LexControl Security Guide

## Deployment Checklist for Production

### Backend (Quarkus) - Railway

**Environment Variables Required:**

```env
# Database - Critical
DB_URL=jdbc:postgresql://[production-db-host]:5432/lexcontrol
DB_USER=[strong-password]
DB_PASSWORD=[strong-password]

# JWT - MUST be generated with SSL keys
JWT_PRIVATE_KEY=[your-private-rsa-key]
JWT_PUBLIC_KEY=[your-public-rsa-key]

# CORS - MUST match frontend URL exactly
CORS_ORIGINS=https://your-frontend-domain.com

# Port
PORT=8080
```

**Generate JWT Keys:**

```bash
# Generate RSA key pair (4096 bits for production)
openssl genrsa -out private.pem 4096
openssl rsa -in private.pem -pubout -out public.pem

# Convert to format expected by application
# Base64 encode both files and set as env vars
```

### Frontend (Vercel)

**Environment Variables:**

```env
VITE_API_URL=https://your-api-domain.railway.app
```

## Security Improvements Applied

### 1. Structured Error Codes ✅
- All API errors now include error codes (e.g., `INVALID_CREDENTIALS`, `NOT_FOUND`)
- Frontend can handle errors programmatically instead of relying on messages
- See: `ApiErrorCode.java`

### 2. Enhanced Logging ✅
- Login/Register events logged for security audit
- Failed authentication attempts tracked
- Resource create/update/delete operations logged
- See: `AuthService.java`, `ClientService.java`

### 3. Rate Limiting Improvements ✅
- IP validation to prevent header spoofing
- X-Forwarded-For and X-Real-IP headers validated
- Rate limiting on auth endpoints (10 requests/minute)
- See: `AuthRateLimitFilter.java`

### 4. Security Headers Added ✅
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1;mode=block
Strict-Transport-Security: max-age=31536000
Content-Security-Policy: default-src 'self'
```

### 5. Error Stack Traces Hidden ✅
- Production: `quarkus.http.error.include-stacktrace=never`
- Prevents information disclosure

## Known Limitations & Recommendations

### JWT Token Storage (Frontend)
⚠️ **Current**: Stored in localStorage (vulnerable to XSS)

**Improvements for Future:**
1. Use httpOnly cookies (requires backend adjustment)
2. Implement CSRF protection
3. Add Content Security Policy headers

### InputSanitizer
ℹ️ **Note**: `InputSanitizer` removes control characters but does NOT provide SQL injection protection.
SQL injection protection comes from **Hibernate parameterized queries**.

### Database Backups
- Railway provides automatic daily backups
- Verify backup settings in Railway dashboard
- Implement point-in-time recovery strategy

### PostgreSQL Configuration
```sql
-- Review these in production:
-- 1. Connection limits
-- 2. SSL enforced
-- 3. Log statement = 'all' for audit trail
-- 4. max_connections set appropriately
```

## Monitoring

### Health Checks
- `/q/health` - Basic health endpoint
- `/q/health/live` - Liveness probe
- `/q/health/ready` - Readiness probe

Configure in Railway for auto-healing.

### Logging
All logs include timestamps and request paths. Monitor for:
- Failed login attempts
- 5xx errors
- Rate limit violations (429)

### Recommended: Add Sentry/DataDog
```properties
quarkus.sentry.enabled=true
quarkus.sentry.dsn=${SENTRY_DSN}
```

## Before Going Live

- [ ] Change CORS_ORIGINS to production domain
- [ ] Generate new JWT RSA keys (don't reuse dev keys)
- [ ] Set strong DB passwords
- [ ] Enable HTTPS/TLS (Railway default)
- [ ] Test auth flow end-to-end
- [ ] Verify rate limiting works
- [ ] Check error responses don't expose stack traces
- [ ] Review security headers in browser DevTools
- [ ] Test with OWASP ZAP or similar

## Incident Response

### If JWT keys are compromised:
1. Generate new RSA keys
2. Update JWT_PRIVATE_KEY and JWT_PUBLIC_KEY in Railway
3. Restart application
4. All existing tokens become invalid (users must re-login)

### If Database is breached:
1. Change DB_USER and DB_PASSWORD
2. Rotate JWT keys (tokens become invalid)
3. Notify users to reset passwords
4. Enable MFA if applicable

---

Last Updated: 2026-03-21
