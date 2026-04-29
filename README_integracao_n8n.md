# Integração com o n8n - Projeto "Cadê a Água?"

## 1. Objetivo da Integração
A integração permite que o sistema notifique automaticamente os utilizadores via canais externos (como e-mail ou WhatsApp) sempre que houver uma alteração crítica no cronograma de abastecimento de água do seu bairro.

## 2. Funcionamento do Fluxo
A lógica de integração segue os seguintes passos:

1.  **Monitorização de Alterações:** O `CronogramaService` monitoriza a criação ou atualização de cronogramas. Se o status de abastecimento for diferente de "Normal", o processo de alerta é iniciado.
2.  **Identificação de Moradores:** O sistema consulta a base de dados para encontrar todos os utilizadores (`Usuario`) que residem no bairro associado à região do cronograma.
3.  **Registo de Notificação:** Para cada morador encontrado, é criada uma entrada na tabela de notificações (`Notificacao`).
4.  **Disparo do Webhook (n8n):** Após o registo interno, o sistema utiliza o `RestTemplate` para enviar um pedido `POST` JSON para o n8n.

## 3. Estrutura dos Dados (Payload)
O n8n recebe um objeto JSON com as seguintes informações do utilizador e do alerta:

* `nome`: Nome completo.
* `email`: Email.
* `telefone`: Contato.
* `bairro`: O bairro afetado pelo rodízio ou manutenção.
* `status_abastecimento`: O novo estado (ex: "Interrompido", "Rodízio").
* `mensagem`: Texto formatado do alerta.
