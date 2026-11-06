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
