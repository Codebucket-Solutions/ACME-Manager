# ACME Manager

This tool helps you automate the provisioning and deployment of X.509 certificates using ACME protocol.

Supported ACME providers:
- Let's Encrypt
- Buypass

## Features
- Certificate Order
- Certificate Validation using DNS or HTTP Challenge
- Certificate Deployment
- List & Search of all Certificates
- Automated DNS Challenge completion using Cloudflare DNS*

`* More providers will be added in future`

## Modules
### ACME Manager Server
It is the control plane for all operations like certificate order, validating, and provisioning.

### ACME Manager Agent
It handles the deployment of certificates on the server. The control plane transfers the certificate and key pair to the agent and deploys it to the server.
