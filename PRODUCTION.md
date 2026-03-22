# LexControl - Production Deployment Guide

## Railway Deployment (Backend + Database)

### 1. Database Setup

**PostgreSQL Configuration:**

```env
# Required environment variables
DB_URL=jdbc:postgresql://[your-railway-db-host]:5432/lexcontrol
DB_USER=[strong-db-user]
DB_PASSWORD=[generate-strong-password]
POSTGRES_DB=lexcontrol
POSTGRES_USER=[same-as-DB_USER]
POSTGRES_PASSWORD=[same-as-DB_PASSWORD]
```

**Backup Strategy:**
- Railway provides automated daily backups
- Verify backup retention in Railway dashboard (minimum 30 days recommended)
- Test restore procedure quarterly

### 2. Backend Service (Quarkus on Railway)

**Required Environment Variables:**

```env
# Database credentials
DB_URL=jdbc:postgresql://[db-host]:5432/lexcontrol
DB_USER=lexcontrol
DB_PASSWORD=[strong-password]

# JWT Configuration
JWT_PRIVATE_KEY=[base64-encoded-private-key]
JWT_PUBLIC_KEY=[base64-encoded-public-key]
JWT_EXPIRATION=3600

# CORS Configuration
CORS_ORIGINS=https://yourdomain.vercel.app

# Server
PORT=8080
```

**Generate JWT Keys (Run once locally):**

```bash
# Generate RSA private key (4096 bits)
openssl genrsa -out private.key 4096

# Extract public key
openssl rsa -in private.key -pubout -out public.key

# Convert to required format (base64)
cat private.key | base64 -w 0 > private.key.b64
cat public.key | base64 -w 0 > public.key.b64

# Copy output to environment variables
```

**Important:** Never commit JWT keys to git! Store in Railway Secrets only.

### 3. Frontend Deployment (Vercel)

**Environment Variables:**

```env
VITE_API_URL=https://[your-railway-api-domain]
```

**Deployment:**
- Connect GitHub repository to Vercel
- Automatic deployments on push to main
- Preview deployments for PRs

---

## Post-Deployment Checklist

### Security
- [ ] JWT keys are unique and secure (not from dev environment)
- [ ] Database password is strong (20+ characters, mixed case, numbers, symbols)
- [ ] CORS_ORIGINS matches frontend domain exactly
- [ ] Security headers enabled in application.properties
- [ ] Error stack traces are hidden (production mode)

### Testing
- [ ] Login/Register flow works end-to-end
- [ ] Rate limiting prevents abuse (10 requests/min on auth endpoints)
- [ ] Database connection verified
- [ ] Health check endpoint responds (/q/health)
- [ ] HTTPS is enforced (browser shows padlock)

### Monitoring
- [ ] Set up health check probes in Railway
- [ ] Monitor application logs for errors
- [ ] Set up alerts for 5xx errors
- [ ] Monitor database connection pool

---

## Common Issues & Troubleshooting

### Issue: 401 Unauthorized on Protected Routes
**Solution:**
1. Verify JWT_PRIVATE_KEY and JWT_PUBLIC_KEY in environment
2. Check JWT token expiration time
3. Verify Authorization header format: `Bearer <token>`

### Issue: CORS errors
**Solution:**
1. Verify CORS_ORIGINS matches exactly (case-sensitive, include protocol)
2. Check browser DevTools for actual origin being requested
3. Enable CORS preflight requests (OPTIONS)

### Issue: Database connection fails
**Solution:**
1. Verify DB_URL format: `jdbc:postgresql://host:5432/dbname`
2. Check DB_USER and DB_PASSWORD
3. Verify database is running and accessible
4. Check network security groups/firewall rules

### Issue: Rate limiting blocks legitimate users
**Solution:**
1. Increase LIMIT in AuthRateLimitFilter if needed (currently 10 req/min)
2. Review logs for IP spoofing or proxy issues
3. Implement IP whitelist if using trusted proxies

---

## Logging & Monitoring

### View Logs
In Railway dashboard:
- Click your service
- Go to "Logs" tab
- Filter by error level if needed

### Important Log Patterns to Monitor
```
INFO - New user registered
INFO - User logged in
WARN - Failed login attempt
WARN - Registration attempt with existing email
ERROR - Unhandled exception
```

### Recommended: Add External Monitoring

**Option 1: Sentry (Error tracking)**
```properties
quarkus.sentry.enabled=true
quarkus.sentry.dsn=${SENTRY_DSN}
quarkus.sentry.environment=production
```

**Option 2: DataDog (APM)**
```properties
quarkus.opentelemetry.enabled=true
```

---

## Scaling & Performance

### Current Limits
- Rate limit: 10 login attempts per 60 seconds per IP
- JWT expiration: 1 hour
- Database connection pool: default (5-10 connections)

### If experiencing issues:
1. **Database**: Increase connection pool in application.properties
2. **API**: Add caching with Redis (future enhancement)
3. **Rate Limit**: Adjust LIMIT constant in AuthRateLimitFilter

---

## Disaster Recovery

### If Production Data is Lost
1. **Last Resort**: Restore from Railway backup
2. **Recommended**: Weekly manual backups to S3
   ```sql
   -- Create backup
   pg_dump -U username dbname > backup.sql
   
   -- Restore
   psql -U username dbname < backup.sql
   ```

### If JWT Keys are Compromised
1. Generate new keys
2. Update JWT_PRIVATE_KEY and JWT_PUBLIC_KEY in Railway
3. Restart service
4. All existing tokens are invalidated (users must re-login)

### If Database Credentials Leak
1. Change DB_USER and DB_PASSWORD in Railway
2. Restart service
3. Generate new JWT keys (due to potential data exposure)
4. Consider password reset requirement for all users

---

## Version Updates

### Quarkus Updates
Monitor: https://quarkus.io/blog/

Check for security patches monthly. Update via `pom.xml`.

### Dependencies
Run periodically:
```bash
mvn versions:display-dependency-updates
```

---

## Support & Documentation

- **Quarkus Docs**: https://quarkus.io/guides/
- **Railway Docs**: https://railway.app/docs
- **Vercel Docs**: https://vercel.com/docs
- **PostgreSQL Docs**: https://www.postgresql.org/docs/

---

Last Updated: 2026-03-21
