<div align="center">

<br>

<img src="https://img.shields.io/badge/⚡_ADVANCED-SobbleMC-blueviolet?style=for-the-badge" alt="Advanced SobbleMC">

# 🌈 SobbleGGWave

**Sistema de ondas de GG com cores rainbow sequenciais no chat.**

<br>

[![Java](https://img.shields.io/badge/Java-8-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.8.8-62B47A?style=for-the-badge&logo=minecraft-education&logoColor=white)](#)
[![Spigot](https://img.shields.io/badge/PandaSpigot-1.8.8-FF6600?style=for-the-badge)](#)
[![Version](https://img.shields.io/badge/v1.0.0-blue?style=for-the-badge)](#)
[![License](https://img.shields.io/github/license/Jojozinho21/SobbleGGWave?style=for-the-badge)](LICENSE)
[![Audit](https://img.shields.io/badge/audit-7.74%2F10-green?style=for-the-badge)](#)

<br>

[Funcionalidades](#-funcionalidades) •
[Quick Start](#-quick-start) •
[Comandos](#-comandos--permissoes) •
[Configuracao](#%EF%B8%8F-configuracao) •
[Como Funciona](#-como-funciona) •
[Arquitetura](#%EF%B8%8F-arquitetura)

<br>

</div>

---

<br>

<div align="center">

### 🏆 Destaques

</div>

<table>
<tr>
<td align="center" width="33%">

**🌈 Cores Rainbow Sequenciais**

GGs coloridos em ordem progressiva — do vermelho ao rosa. Distribuicao proporcional ao tamanho da onda.

</td>
<td align="center" width="33%">

**⏱️ Onda por Contagem + Tempo**

A onda encerra ao atingir o limite de GGs OU quando o tempo expira — o que vier primeiro.

</td>
<td align="center" width="33%">

**🛡️ Anti-Spam Inteligente**

Cooldown per-player com feedback em tempo real. Mostra segundos restantes para o proximo GG.

</td>
</tr>
</table>

<br>

---

<br>

## ✨ Funcionalidades

<table>
<tr>
<td width="50%">

**Core**
- 🌈 Cores rainbow sequenciais (configuravel)
- 📊 Progresso visivel em cada GG `[3/20]`
- ⏱️ Onda limitada por contagem E tempo
- 🛡️ Cooldown anti-spam per-player
- 🎯 Trigger word configuravel (padrao: "gg")

</td>
<td width="50%">

**UX**
- 📢 Anuncios de inicio/fim da onda
- 🥇 Destaque no primeiro e ultimo GG
- 🏆 Top participante mostrado no fim
- 💬 Chat normal nao e afetado
- 🔄 Tab-complete contextual por permissao

</td>
</tr>
</table>

<br>

---

<br>

## 🚀 Quick Start

```bash
# 1. Clone o repositorio
git clone https://github.com/Jojozinho21/SobbleGGWave.git

# 2. Compile com Maven
cd SobbleGGWave
mvn clean package

# 3. Copie o JAR para seu servidor
cp target/sobbleggwave-1.0.0.jar /seu-servidor/plugins/

# 4. Reinicie o servidor e use /ggwave iniciar
```

<br>

---

<br>

## 📋 Comandos & Permissoes

### Comandos

| Comando | Descricao | Permissao |
|---------|-----------|-----------|
| `/ggwave iniciar` | Inicia uma onda de GG | `sobbleggwave.admin` |
| `/ggwave parar` | Para a onda ativa | `sobbleggwave.admin` |
| `/ggwave reload` | Recarrega a config | `sobbleggwave.admin` |
| `/ggwave status` | Mostra status da onda | Nenhuma |

### Permissoes

| Permissao | Descricao | Default |
|-----------|-----------|---------|
| `sobbleggwave.admin` | Iniciar, parar e recarregar ondas | `op` |

<br>

---

<br>

## 🎨 Como Funciona

### Distribuicao de Cores

As cores sao distribuidas **sequencialmente** ao longo da onda. Com 6 cores e onda de 20 GGs:

```
GG  1-3  → &c&l (Vermelho Bold)
GG  4-6  → &6&l (Laranja Bold)
GG  7-10 → &e&l (Amarelo Bold)
GG 11-13 → &a&l (Verde Bold)
GG 14-16 → &b&l (Ciano Bold)
GG 17-20 → &d&l (Rosa Bold)
```

Formula: `colorIndex = ((ggNumber - 1) * numColors) / waveSize`

### Fluxo da Onda

```
Admin: /ggwave iniciar
    ↓
[GGWave] Uma onda de GG comecou! Digite "gg" no chat! (20 GGs, 60s)
    ↓
[GGWave] >>> Steve abriu a onda com o primeiro GG!
[GGWave] [1/20] §c§l Steve: GG!
[GGWave] [2/20] §c§l Alex: GG!
[GGWave] [3/20] §c§l Notch: GG!
[GGWave] [4/20] §6§l Steve: GG!     ← cor muda
    ...
[GGWave] [20/20] §d§l Alex: GG!
[GGWave] <<< Alex fechou a onda com o GG final!
[GGWave] Onda encerrada! 20 GGs enviados. Maior participante: Steve com 5 GGs!
```

<br>

---

<br>

## ⚙️ Configuracao

```yaml
# Prefix para mensagens do plugin
prefix: '&8[&b&lGGWave&8] '

# Trigger word (case-insensitive, match exato)
trigger-word: 'gg'

# Configuracoes da onda
wave:
  size: 20          # Max GGs (min: 5, max: 200)
  timeout: 60       # Segundos (min: 10, max: 600)
  cooldown: 3       # Cooldown per-player em segundos (min: 1, max: 30)

# Cores sequenciais (aplicadas em ordem)
colors:
  - '&c&l'    # Vermelho Bold
  - '&6&l'    # Laranja Bold
  - '&e&l'    # Amarelo Bold
  - '&a&l'    # Verde Bold
  - '&b&l'    # Ciano Bold
  - '&d&l'    # Rosa Bold

# Formato do broadcast de GG
gg-format: '{color}{player}: GG!'

# Todas as mensagens sao configuraveis
messages:
  wave-start: '&aUma onda de GG comecou! Digite &f&l{trigger}&a no chat! ...'
  wave-end-count: '&aOnda encerrada! {total} GGs enviados. ...'
  # ... (veja config.yml completo)
```

<br>

---

<br>

## 🏗️ Arquitetura

```
com.sobblemc.sobbleggwave/
├── SobbleGGWavePlugin.java   — Lifecycle + config validation
├── WaveManager.java           — Wave state + GG processing (synchronized)
├── WaveBroadcaster.java       — Pure message formatter (no state)
├── GGWaveCommand.java         — Command handler + tab completer
├── ChatListener.java          — AsyncPlayerChatEvent interceptor
└── MessageUtil.java           — Color translation utility
```

| Classe | Linhas | Responsabilidade |
|--------|--------|-----------------|
| SobbleGGWavePlugin | 106 | Lifecycle, config validation |
| WaveManager | 240 | State, cooldowns, colors, wave lifecycle |
| WaveBroadcaster | 60 | Message formatting (pure, no state) |
| GGWaveCommand | 140 | Commands + tab completion |
| ChatListener | 44 | Event interception |
| MessageUtil | 41 | Color translation |

**Auditado em 7.74/10 — PRODUCTION_READY**

<br>

---

<br>

## 📝 Licenca

Distribuido sob a licenca MIT. Veja [LICENSE](LICENSE) para mais informacoes.

<br>

---

<div align="center">

**Feito com ❤️ para o SobbleMC**

</div>
