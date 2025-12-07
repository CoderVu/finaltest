# Hướng dẫn thiết lập CI/CD với GitHub Actions

## Tổng quan

Workflow CI/CD này tự động:
1. **Build** project Maven
2. **Test** với Selenium (headless Chrome)
3. **Deploy** lên server thuê qua SSH
4. **Deploy Docker** services (Selenium Grid, MySQL, Automation Runner)

## Yêu cầu

### Trên GitHub Repository

1. **Secrets cần thiết** (Settings → Secrets and variables → Actions):
   - `SSH_PRIVATE_KEY`: Private key SSH để kết nối server
   - `SERVER_HOST`: Địa chỉ IP hoặc domain của server (ví dụ: `192.168.1.100` hoặc `server.example.com`)
   - `SERVER_USER`: Username SSH (ví dụ: `root` hoặc `ubuntu`)
   - `DEPLOY_PATH`: Đường dẫn deploy trên server (ví dụ: `/opt/automation`)

### Trên Server

1. **Cài đặt cơ bản**:
   ```bash
   # Cài đặt Java 17
   sudo apt update
   sudo apt install openjdk-17-jdk -y
   
   # Cài đặt Maven
   sudo apt install maven -y
   
   # Cài đặt Docker và Docker Compose
   sudo apt install docker.io docker-compose -y
   sudo systemctl enable docker
   sudo systemctl start docker
   
   # Thêm user vào docker group (nếu không dùng root)
   sudo usermod -aG docker $USER
   ```

2. **Cấu hình SSH**:
   ```bash
   # Tạo SSH key pair (nếu chưa có)
   ssh-keygen -t rsa -b 4096 -C "github-actions"
   
   # Copy public key vào server
   cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
   
   # Cấu hình SSH (nếu cần)
   sudo nano /etc/ssh/sshd_config
   # Đảm bảo:
   # - PubkeyAuthentication yes
   # - PasswordAuthentication no (khuyến nghị)
   ```

3. **Tạo thư mục deploy**:
   ```bash
   sudo mkdir -p /opt/automation/{app,backup,logs}
   sudo chown -R $USER:$USER /opt/automation
   ```

## Cấu hình GitHub Secrets

### Bước 1: Tạo SSH Key Pair

Trên máy local hoặc server:

```bash
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy" -f ~/.ssh/github_actions_deploy
```

### Bước 2: Copy Public Key vào Server

```bash
# Copy public key vào server
ssh-copy-id -i ~/.ssh/github_actions_deploy.pub user@your-server-ip

# Hoặc copy thủ công
cat ~/.ssh/github_actions_deploy.pub | ssh user@your-server-ip "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

### Bước 3: Thêm Secrets vào GitHub

1. Vào repository → **Settings** → **Secrets and variables** → **Actions**
2. Thêm các secrets sau:

| Secret Name | Value | Ví dụ |
|------------|-------|-------|
| `SSH_PRIVATE_KEY` | Nội dung file private key | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `SERVER_HOST` | IP hoặc domain server | `192.168.1.100` |
| `SERVER_USER` | Username SSH | `root` hoặc `ubuntu` |
| `DEPLOY_PATH` | Đường dẫn deploy | `/opt/automation` |

**Lưu ý**: Copy toàn bộ nội dung file private key (bao gồm `-----BEGIN...` và `-----END...`)

## Cấu hình Environment Variables trên Server

Nếu cần environment variables cho Docker Compose (như trong `docker-compose.yml`):

```bash
# Tạo file .env trên server
cd /opt/automation/app
nano .env
```

Thêm các biến:
```env
AZURE_STORAGE_ACCOUNT_KEY=your-key
FACEBOOK_CLIENT_ID=your-id
FACEBOOK_CLIENT_SECRET=your-secret
GOOGLE_CLIENT_ID=your-id
GOOGLE_CLIENT_SECRET=your-secret
```

Hoặc export trước khi chạy docker-compose:
```bash
export AZURE_STORAGE_ACCOUNT_KEY="your-key"
export FACEBOOK_CLIENT_ID="your-id"
# ... các biến khác
```

## Workflow Triggers

Workflow sẽ chạy tự động khi:
- **Push** vào branch `main`, `master`, hoặc `develop`
- **Pull Request** vào `main` hoặc `master` (chỉ build/test, không deploy)
- **Manual trigger** (Actions → CI/CD Pipeline → Run workflow)

## Manual Deployment

### Option 1: Qua GitHub Actions UI

1. Vào **Actions** tab
2. Chọn workflow **CI/CD Pipeline**
3. Click **Run workflow**
4. Chọn:
   - Branch: `main` hoặc `master`
   - Environment: `staging` hoặc `production`
   - Skip tests: `true` hoặc `false`

### Option 2: Chạy script trên server

```bash
# Clone repository (lần đầu)
cd /opt/automation
git clone https://github.com/your-username/your-repo.git app

# Hoặc pull latest changes
cd /opt/automation/app
git pull origin main

# Chạy deployment script
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

## Kiểm tra Deployment

### Kiểm tra containers

```bash
cd /opt/automation/app
docker-compose ps
docker-compose logs
```

### Kiểm tra services

- **Selenium Grid UI**: `http://your-server-ip:4444/ui`
- **Automation Runner**: `http://your-server-ip:8080`
- **MySQL**: `your-server-ip:3307`

### Xem logs

```bash
# Logs của tất cả services
docker-compose logs -f

# Logs của service cụ thể
docker-compose logs -f automation_runner
docker-compose logs -f selenium-hub
```

## Troubleshooting

### Lỗi SSH Connection

```bash
# Test SSH connection
ssh -i ~/.ssh/github_actions_deploy user@your-server-ip

# Kiểm tra SSH service
sudo systemctl status sshd

# Xem SSH logs
sudo tail -f /var/log/auth.log
```

### Lỗi Docker Permission

```bash
# Thêm user vào docker group
sudo usermod -aG docker $USER
newgrp docker

# Hoặc chạy với sudo
sudo docker-compose up -d
```

### Lỗi Port đã được sử dụng

```bash
# Kiểm tra port đang được sử dụng
sudo netstat -tulpn | grep :4444
sudo netstat -tulpn | grep :8080

# Dừng service đang dùng port
sudo systemctl stop service-name
# Hoặc thay đổi port trong docker-compose.yml
```

### Lỗi Build/Test trong GitHub Actions

- Kiểm tra Java version: Đảm bảo Java 17 được cài đặt
- Kiểm tra Maven cache: Có thể clear cache và rebuild
- Xem logs chi tiết trong Actions tab

## Tùy chỉnh Workflow

### Thay đổi branch trigger

Sửa file `.github/workflows/ci-cd.yml`:

```yaml
on:
  push:
    branches:
      - your-branch-name
```

### Thay đổi test configuration

Sửa step "Run tests":

```yaml
- name: Run tests
  run: |
    mvn test -Dheadless=true -Dbrowser=chrome -Psmoke-staging
```

### Thêm notification

Thêm step để gửi notification (Slack, Discord, Email):

```yaml
- name: Notify Slack
  if: always()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

## Security Best Practices

1. **Không commit secrets**: Luôn dùng GitHub Secrets
2. **SSH Key rotation**: Đổi SSH key định kỳ
3. **Firewall**: Chỉ mở port cần thiết (22, 4444, 8080, 3307)
4. **User permissions**: Dùng user riêng thay vì root nếu có thể
5. **Backup**: Giữ backup của deployments

## Backup và Rollback

### Manual Backup

```bash
cd /opt/automation
tar -czf backup-$(date +%Y%m%d).tar.gz app/
```

### Rollback

```bash
# Dừng services
cd /opt/automation/app
docker-compose down

# Restore từ backup
cd /opt/automation/backup
tar -xzf backup-YYYYMMDD-HHMMSS.tar.gz -C ../app/

# Khởi động lại
cd ../app
docker-compose up -d
```

## Liên hệ và Hỗ trợ

Nếu gặp vấn đề, kiểm tra:
1. GitHub Actions logs
2. Server logs: `docker-compose logs`
3. SSH logs: `/var/log/auth.log`
4. System logs: `journalctl -u docker`

