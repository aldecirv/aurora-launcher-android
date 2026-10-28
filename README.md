# Aurora Launcher (Android nativo)

Launcher minimalista com lista alfabética, busca, temas e personalização.

## Novidades da versão 1.4

- **Tema claro / escuro / automático** (segue o sistema), aplicado em todas as telas e persistido.
- **Busca em tempo real** por nome, com botão de limpar e mensagem de "nenhum resultado".
- **Ordenação** A–Z / Z–A pelo botão ao lado da busca.
- **Atualização automática** da lista quando um app é instalado, removido ou atualizado.
- **Tamanho dos ícones** (28–64 dp) e **zoom da interface** (85–130%).
- **Colunas** (1–5) e **linhas por tela** (3–8) para o modo grade.
- **Estilo dos ícones**: original, monocromático ou escala de cinza.
- **Formato dos ícones**: original, arredondado ou círculo.
- **Tela de configurações** completa com **Restaurar padrões**.
- **Fundo personalizável**: tema, **cor sólida** (qualquer cor) ou **papel de parede desfocado**, com controles de desfoque (0–25) e sobreposição (0–80%) para manter a legibilidade.
- **Cor de destaque personalizável**: 9 predefinições + seletor RGB para escolher **qualquer cor** (aplicada ao índice lateral, cabeçalhos, busca, sliders, botões e ícones monocromáticos).

## Estrutura

```
app/src/main/java/org/aurora/launcher/
├── AuroraApp.kt        aplica o tema salvo antes das Activities
├── Prefs.kt            preferências (SharedPreferences)
├── Accent.kt           cor de destaque + contraste automático
├── Background.kt       fundo por cor/papel de parede + desfoque e cache
├── ColorPickerDialog.kt seletor RGB de qualquer cor
├── IconLoader.kt       rasteriza/cacheia ícones com filtro e máscara
├── MainActivity.kt     busca, lista, índice e ordenação
├── AppAdapter.kt       lista/grade com cabeçalhos de letra
├── SettingsActivity.kt tela de configurações
└── AppInfo.kt          modelo de aplicativo
```

## Limitações (Android)

- O **formato** e o **filtro** são aplicados na cópia desenhada pelo launcher; alguns
  ícones adaptativos (camadas) podem perder detalhes ao circular/arredondar.
- Launchers **não alteram** o ícone real do app no sistema — só como ele é exibido aqui.
- Ícones de apps com proteção (DRM/assinatura) continuam exibidos normalmente, mas
  não podem ser recoloridos de forma perfeita.
- "Linhas por tela" só tem efeito no modo grade (colunas > 1).
- **Papel de parede animado** (wallpaper ao vivo) é exibido pelo sistema, sem desfoque; o desfoque funciona em imagens estáticas. A sobreposição garante a leitura nos dois casos.
- A tela de **Configurações** mantém o fundo do tema para preservar a legibilidade dos textos.

## Gerar o APK (GitHub Actions)

O repositório já tem o workflow `.github/workflows/build.yml`:

1. `git push` na branch `main`
2. **Actions → Build APK** → aguarde
3. **Artifacts → aurora-launcher-debug** → baixe o `.apk`

## Instalar e testar

1. Instale o APK (permita "apps desconhecidos").
2. Abra **Aurora Launcher** pelo menu (ícone do app).
3. Teste:
   - digite na busca e use o **X** para limpar;
   - toque no ícone de **ordenar** e alterne A–Z / Z–A;
   - abra **Configurações** (engrenagem) e mude tema, tamanhos, colunas, estilo e formato;
   - volte e confirme que tudo foi aplicado;
   - **Restaurar padrões** volta ao estado inicial.
4. Defina como tela inicial: **Configurações → Apps → Apps padrão → App de início → Aurora Launcher**.

Licença: MIT.
