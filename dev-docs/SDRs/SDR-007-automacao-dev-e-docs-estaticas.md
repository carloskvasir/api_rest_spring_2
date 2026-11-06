# SDR-007: Automação de Desenvolvimento (Seeds) e Documentação Estática

**Data:** 2026-10-01
**Status:** Aceito

## 1. Contexto e Motivação
Com a infraestrutura de backend e os padrões de API definidos (SDRs anteriores), identificamos dois gargalos de usabilidade (Developer Experience e Avaliação):
1. Testar uma API vazia é improdutivo; o desenvolvedor precisaria injetar dados manualmente a cada inicialização para validar endpoints de listagem e busca.
2. Para que um avaliador externo visualize o contrato da API, ele precisaria obrigatoriamente rodar os containers Docker na própria máquina.

## 2. Decisão
Para resolver esses problemas, decidimos implementar duas soluções de automação:
- **Data Seeder:** Criação da classe `DataSeeder` (implementando `CommandLineRunner`), que intercepta o momento de *startup* da aplicação para popular a base em memória com 5 registros clássicos (caso a base esteja vazia).
- **Documentação Estática (GitHub Pages):** Exportação do JSON resultante do OpenAPI gerado dinamicamente para um arquivo físico (`docs/openapi.json`), atrelado a um arquivo `index.html` consumindo os pacotes estáticos via CDN do Swagger UI.

## 3. System Design Review (Análise de Impacto)
- **Desempenho e Acoplamento:** O Seeder possui impacto imperceptível no startup (O(1)) e verifica previamente se existem dados, evitando duplicações desnecessárias durante o *Hot Reload* do Docker.
- **Portabilidade:** A documentação na pasta `/docs` viabiliza hospedagem gratuita em ferramentas de CDNs estáticas (como GitHub Pages), desacoplando a infraestrutura de visualização de contrato da infraestrutura de execução da API.

## 4. Consequências
- **Positivos:** Facilita massivamente o teste manual para novos desenvolvedores. Garante que qualquer pessoa possa validar o formato da API e as decisões através do navegador de internet, sem baixar código fonte.
- **Negativos:** A documentação estática gerada na pasta `/docs` precisa ser reexportada manualmente caso hajam futuras alterações nos DTOs ou Models do projeto para se manter sincronizada.
