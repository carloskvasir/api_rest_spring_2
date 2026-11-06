#!/bin/bash
set -e

if [[ -n $(git status -s) ]]; then
  echo "❌ Erro: Seu repositório tem arquivos não commitados."
  exit 1
fi

NEXT_VERSION=$(npx --yes git-cliff --bumped-version | sed 's/v//g')
echo "🚀 Iniciando processo de Release: v$NEXT_VERSION"

echo "📦 1/5 Atualizando pom.xml..."
./mvnw versions:set -DnewVersion=$NEXT_VERSION -DgenerateBackupPoms=false -q

echo "📄 2/5 Atualizando Swagger (OpenAPI)..."
sed -i "s/\.version(\".*\")/.version(\"$NEXT_VERSION\")/g" src/main/java/com/api/livros/config/OpenApiConfig.java
if [ -f "docs/openapi.json" ]; then
  sed -i "s/\"version\":\"[0-9]*\.[0-9]*\.[0-9]*\"/\"version\":\"$NEXT_VERSION\"/g" docs/openapi.json
fi

echo "📝 3/5 Gerando CHANGELOG.md Híbrido..."
cat << 'JS_EOF' > bin/generate_changelog.js
const fs = require('fs');
const { execSync } = require('child_process');

const contextRaw = execSync("npx --yes git-cliff --bump --context", { encoding: 'utf-8' });
const context = JSON.parse(contextRaw);

let md = `# Changelog\n\n`;
md += `Todos os marcos e mudanças notáveis neste projeto serão documentados neste arquivo.\n`;
md += `O formato baseia-se no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e é gerado dinamicamente.\n\n`;

context.forEach((release, index) => {
    if (!release.version) return;
    const vName = release.version.replace('v', '');
    const date = new Date(release.timestamp * 1000).toISOString().split('T')[0];
    
    if (index === 0) {
        md += `## [${vName}](https://github.com/carloskvasir/api_rest_spring/releases/tag/${release.version}) - ${date}\n`;
        
        const groups = {};
        release.commits.forEach(commit => {
            if (!groups[commit.group]) groups[commit.group] = [];
            groups[commit.group].push(commit.message);
        });
        
        Object.keys(groups).forEach(g => {
            md += `### ${g}\n`;
            groups[g].forEach(msg => {
                md += `- ${msg}\n`;
            });
        });
        md += `\n`;
    } else {
        md += `## [${vName}](https://github.com/carloskvasir/api_rest_spring/releases/tag/${release.version}) - ${date}\n`;
        md += `> *(Consulte a tag acima para ver as notas completas)*\n\n`;
    }
});

fs.writeFileSync('CHANGELOG.md', md);
JS_EOF

node bin/generate_changelog.js

echo "🏷️  4/5 Criando commit e TAG no Git..."
git add pom.xml CHANGELOG.md bin/release.sh bin/generate_changelog.js src/main/java/com/api/livros/config/OpenApiConfig.java docs/openapi.json
git commit -m "chore: release da versao $NEXT_VERSION"
git tag -a "v$NEXT_VERSION" -m "Release v$NEXT_VERSION"

echo "☁️  5/5 Publicando no GitHub..."
git push origin main
git push origin v$NEXT_VERSION

npx --yes git-cliff --latest --strip header > .github_release_notes.md
echo -e "\n---\n📖 **Histórico Completo:** Acesse nosso [CHANGELOG.md](https://github.com/carloskvasir/api_rest_spring/blob/v$NEXT_VERSION/CHANGELOG.md)." >> .github_release_notes.md

gh release create v$NEXT_VERSION -t "Release v$NEXT_VERSION" -F .github_release_notes.md
rm .github_release_notes.md

echo "=========================================================="
echo "✅ Release v$NEXT_VERSION publicada com sucesso no GitHub!"
