# PointPal - Slack Recognition Bot

A Slack bot that enables team members to give recognition points to each other, fostering a culture of appreciation and recognition in the workplace.

## Features

- Give recognition points to team members
- View your current points and remaining points to give
- View top 10 users with highest points for the current month
- Monthly point reset
- Rate limiting to prevent abuse
- Secure transaction validation
- User deactivation management

## Commands

- `/i-want-to-give @username <amount>` - Give points to someone
- `/me` - Show your current point balance
- `/top-ten` - Show top 10 users with highest points this month
- `/help` - Show help message

## Security Features

- Rate limiting (configurable requests per minute)
- Transaction validation
- Prevention of self-donation
- Suspicious pattern detection
- Large transfer restrictions
- Recipient-based limits
- User deactivation support

## User Management

- Active users can give and receive points
- Deactivated users cannot:
  - Give points to others
  - Receive points from others
  - Appear in top-ten rankings
- Only admins can deactivate users
- Deactivated users' historical data is preserved

## Configuration

The application can be configured through environment variables or application.properties:

### Application Settings

```
APP_NAME=PointPal                    # Application name
PORT=8080                           # Server port
```

### Database Configuration

```
JDBC_DATABASE_URL=                  # Database URL
JDBC_DATABASE_USERNAME=             # Database username
JDBC_DATABASE_PASSWORD=             # Database password
```

### Slack Configuration

```
SLACK_SIGNING_SECRET=               # Slack app signing secret
SLACK_BOT_TOKEN=                    # Slack bot token
SLACK_APP_TOKEN=                    # Slack app token
SLACK_DONATE_CHANNEL=coin_pool      # Channel for point donations
SLACK_APP_NAME=PointPal Bot         # Bot display name
```

### Point System Configuration

```
app.max-points-per-month=50         # Maximum points a user can give per month
app.rate-limit.requests-per-minute=10 # Rate limit for API requests
```

### Git Secrets

To store sensitive information securely, use Git secrets. Here's how to set up and use them:

1. Install git-secrets:

   ```bash
   # macOS
   brew install git-secrets

   # Linux
   sudo apt-get install git-secrets
   ```

2. Initialize git-secrets in your repository:

   ```bash
   git secrets --install
   git secrets --register-aws
   ```

3. Add the following secrets to your repository:

   ```bash
   # Database credentials
   git secrets --add 'JDBC_DATABASE_URL=.*'
   git secrets --add 'JDBC_DATABASE_USERNAME=.*'
   git secrets --add 'JDBC_DATABASE_PASSWORD=.*'

   # Slack credentials
   git secrets --add 'SLACK_SIGNING_SECRET=.*'
   git secrets --add 'SLACK_BOT_TOKEN=.*'
   git secrets --add 'SLACK_APP_TOKEN=.*'
   ```

4. Create a `.env` file for local development (this file should be in .gitignore):

   ```
   # Database
   JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/thankyou
   JDBC_DATABASE_USERNAME=your_username
   JDBC_DATABASE_PASSWORD=your_password

   # Slack
   SLACK_SIGNING_SECRET=your_signing_secret
   SLACK_BOT_TOKEN=your_bot_token
   SLACK_APP_TOKEN=your_app_token
   ```

5. For production deployment, use your platform's secrets management:

   - Heroku: Config Vars
   - AWS: Secrets Manager
   - GCP: Secret Manager
   - Azure: Key Vault

6. Configure GitHub Secrets for CI/CD:

   a. Go to your GitHub repository
   b. Click on "Settings" > "Secrets and variables" > "Actions"
   c. Click "New repository secret"
   d. Add the following secrets:

   ```
   # Database
   JDBC_DATABASE_URL
   JDBC_DATABASE_USERNAME
   JDBC_DATABASE_PASSWORD

   # Slack
   SLACK_SIGNING_SECRET
   SLACK_BOT_TOKEN
   SLACK_APP_TOKEN
   ```

   e. In your GitHub Actions workflow file, use the secrets like this:

   ```yaml
   env:
     JDBC_DATABASE_URL: ${{ secrets.JDBC_DATABASE_URL }}
     JDBC_DATABASE_USERNAME: ${{ secrets.JDBC_DATABASE_USERNAME }}
     JDBC_DATABASE_PASSWORD: ${{ secrets.JDBC_DATABASE_PASSWORD }}
     SLACK_SIGNING_SECRET: ${{ secrets.SLACK_SIGNING_SECRET }}
     SLACK_BOT_TOKEN: ${{ secrets.SLACK_BOT_TOKEN }}
     SLACK_APP_TOKEN: ${{ secrets.SLACK_APP_TOKEN }}
   ```

Remember:

- Never commit sensitive information directly to the repository
- Use environment variables or secrets management in production
- Keep your `.env` file local and add it to `.gitignore`
- Regularly rotate secrets and tokens
- Use different secrets for development and production environments
- GitHub Secrets are encrypted and only exposed to selected GitHub Actions
- Secrets are not visible in logs or to users without proper permissions

## Heroku Deployment

1. Create a new Heroku app
2. Set up the PostgreSQL add-on
3. Configure environment variables:

```bash
heroku config:set APP_NAME="PointPal"
heroku config:set SLACK_SIGNING_SECRET="your-signing-secret"
heroku config:set SLACK_BOT_TOKEN="your-bot-token"
heroku config:set SLACK_APP_TOKEN="your-app-token"
heroku config:set SLACK_DONATE_CHANNEL="coin_pool"
heroku config:set SLACK_APP_NAME="PointPal Bot"
heroku config:set app.rate-limit.requests-per-minute=10
heroku config:set app.max-points-per-month=50
```

## Local Development

1. Clone the repository
2. Set up PostgreSQL database
3. Create a Slack app and get the necessary tokens
4. Create `application-local.properties` with your configuration
5. Run the application:

```bash
./mvnw spring-boot:run
```

### Running with .env File

1. Create a `.env` file in the project root:

```bash
# Database Configuration
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/thankyou
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=postgres

# Slack Configuration
SLACK_SIGNING_SECRET=your_signing_secret
SLACK_BOT_TOKEN=your_bot_token
SLACK_APP_TOKEN=your_app_token
SLACK_DONATE_CHANNEL=coin_pool
SLACK_APP_NAME=PointPal Bot

# Application Settings
APP_NAME=PointPal
PORT=8080

# Point System Configuration
app.max-points-per-month=50
app.rate-limit.requests-per-minute=10
```

2. Install the required dependencies:

```bash
# macOS
brew install direnv

# Linux
sudo apt-get install direnv
```

3. Add direnv to your shell (add to ~/.zshrc or ~/.bashrc):

```bash
eval "$(direnv hook zsh)"  # for zsh
# or
eval "$(direnv hook bash)" # for bash
```

4. Allow direnv in your project directory:

```bash
direnv allow .
```

5. Run the application:

```bash
./mvnw spring-boot:run
```

The application will automatically load the environment variables from the `.env` file.

Note: Make sure to add `.env` to your `.gitignore` file to prevent committing sensitive information:

```bash
echo ".env" >> .gitignore
```

## Security Considerations

- All sensitive tokens are stored as environment variables
- Rate limiting prevents API abuse
- Transaction validation ensures fair point distribution
- Large transfers require user history
- Maximum 3 transfers to the same user per month
- Self-donation prevention
- Suspicious pattern detection
- User deactivation for security and compliance

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
