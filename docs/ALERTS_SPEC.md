# Alertas & Notificações - Especificação

Objetivo: implementar sistema de alertas local e push para MinerControl.

Resumo das funcionalidades:
- Alertas locais (notificações) para: temperatura alta, miner offline, novo bloco válido, queda de hashrate.
- Push (opcional): integração com Firebase Cloud Messaging (FCM) para enviar notificações remotas (opcional, opt-in).
- Thresholds configuráveis por utilizador (por miner): temperatura, hashrate mínimo, offline timeout.
- Histórico de alertas gravado em DB para auditoria.
- Notificações persistentes/ações rápidas (abrir detalhe do miner, desativar alerta).

Dependências / permissões necessárias:
- WorkManager (para schedulers / retries)
- AndroidX Core / NotificationCompat
- Room (para histórico e regras de alertas) ou DataStore para regras simples
- Firebase Cloud Messaging (opcional) + google-services.json
- Permissões: INTERNET, FOREGROUND_SERVICE (se usar ForegroundService)

Arquitetura proposta:
- alerts/Alert.kt - modelo de alerta
- alerts/AlertRule.kt - regras configuráveis por miner
- alerts/AlertRepository.kt - persistência de regras e histórico (Room)
- alerts/AlertService.kt - serviço que avalia regras e dispara notificações (poderá ser WorkManager ou ForegroundService)
- alerts/NotificationHelper.kt - criação de canais de notificação e helpers
- ui/screens/AlertsSettings.kt - UI para configurar thresholds por miner

Fluxo básico:
1. UDP Listener recebe payload e atualiza modelo do miner.
2. Quando atualizar miner, notifica AlertService/AlertRepository para avaliar regras.
3. Se regra violada, criar evento de alerta, guardar no histórico e disparar notificação local.
4. (Opcional) Se configurado, enviar evento para backend/FCM para envio push.

Notas de implementação:
- Evitar spam de notificações: cooldown por regra (ex.: 10 min).
- Mostrar aviso no UI com indicação de quando o último alerta ocorreu.
- Forçar opt-in para envio remoto (FCM) e documentar privacidade.

Tarefas iniciais (MVP):
- Scaffold dos modelos e repo (Room)
- NotificationHelper e canal
- Avaliação simples de rule: temp > threshold, miner offline
- UI básica de thresholds

Data: 2025-09-11
Autor: Prismas33 / implementation scaffold
