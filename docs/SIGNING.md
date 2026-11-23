# MKSU 发布签名与管理器认证

MKSU 管理器使用独立的 applicationId `com.zeroedx.mksu`，发布 APK 必须使用维护者长期持有的证书签名。内核模块只接受同一包名和同一证书，构建脚本不会回退到 debug 或上游签名。

## GitHub Actions Secrets

在仓库或组织 Secrets 中配置以下四项：

| Secret | 内容 |
| --- | --- |
| `KEYSTORE` | 发布密钥库文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库口令 |
| `KEY_ALIAS` | 发布密钥别名 |
| `KEY_PASSWORD` | 私钥口令 |

Base64 编码示例：

```bash
base64 -w 0 release.jks
```

私钥文件、口令和 Base64 内容不得提交到仓库，也不得写入 Issue、PR 或构建日志。

## 公开证书指纹

CI 会从 `KEYSTORE` 导出证书，计算证书 DER 的 SHA-256 和长度，并分别传给管理器与内核构建。需要人工核对时，可在可信环境执行：

```bash
keytool -exportcert \
  -alias "$KEY_ALIAS" \
  -keystore release.jks \
  -storepass "$KEYSTORE_PASSWORD" \
  -file release-cert.der

sha256sum release-cert.der
stat -c%s release-cert.der
```

- 管理器构建：传入 `-PMKSU_CERT_SHA256=<sha256>`，用于运行时自检。
- 内核构建：传入 `KSU_EXPECTED_SIZE=0x<长度>` 和 `KSU_EXPECTED_HASH=<sha256>`，用于 `check_v2_signature()`。
- 本地构建发布 APK 时，还需通过 `-PKEYSTORE_FILE=<绝对路径>`、`-PKEYSTORE_PASSWORD`、`-PKEY_ALIAS`、`-PKEY_PASSWORD` 提供密钥库；缺少任一项会直接失败。

## 发布产物

正式发行只打包 `arm64-v8a` 管理器 APK。日常 `main`/`dev` 分支构建仍保留上游的 `arm64-v8a` 与 `x86_64` 产物体系。发布 APK 使用 APK Signature Scheme v2，不使用 v1/v3/v4。
