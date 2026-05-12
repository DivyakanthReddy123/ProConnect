# SMS and Email Notification Setup

This update adds external email/SMS delivery for two app events:

1. A user sends a connection request.
2. A recruiter posts a new job.

The app still creates the in-app notification first. External delivery is best-effort: if email/SMS fails, the app action still succeeds.

## What changed

Backend:
- Added `NotificationDeliveryService`.
- Added email delivery through Spring Mail.
- Added SMS delivery through Twilio REST API.
- Added `JOB_POST` notification type.
- When a recruiter posts a job, professional users receive an in-app job alert and optional email/SMS.
- When a connection request is sent, the receiver gets optional email/SMS.

Frontend:
- Notifications page now has a `Job alerts` section for new job-post notifications.

## Enable email

In `back-end/linkedin-clone/src/main/resources/application.properties`, set:

```properties
app.notifications.email.enabled=true
app.notifications.email.from=your_email@gmail.com
spring.mail.username=your_email@gmail.com
spring.mail.password=your_gmail_app_password
```

For Gmail, use an App Password, not your normal Gmail password.

## Enable SMS

In `application.properties`, set:

```properties
app.notifications.sms.enabled=true
twilio.account-sid=YOUR_TWILIO_ACCOUNT_SID
twilio.auth-token=YOUR_TWILIO_AUTH_TOKEN
twilio.from-number=+1234567890
```

User phone numbers must include country code, for example:

```text
+14155552671
```

## Safe defaults

Both are disabled by default:

```properties
app.notifications.email.enabled=false
app.notifications.sms.enabled=false
```

So the app runs normally without provider credentials.
