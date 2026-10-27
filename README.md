# Aurora Launcher (Android nativo)

Launcher minimalista com lista alfabética e índice lateral — identidade visual do Aurora.

## Como gerar o APK (sem instalar Android SDK)

O projeto já vem com um workflow do GitHub Actions:

1. Crie um repositório no GitHub e envie esta pasta:
   ```bash
   cd aurora-launcher-android
   git init && git add -A
   git commit -m "Aurora Launcher"
   git branch -M main
   git remote add origin https://github.com/SEU_USUARIO/aurora-launcher-android.git
   git push -u origin main
   ```
2. No GitHub: aba **Actions** → **Build APK** → aguarde o verde.
3. Abra a execução → **Artifacts** → baixe **aurora-launcher-debug.zip**.
4. Descompacte o `.apk`.

## Instalar no celular

1. Ative **"Instalar apps desconhecidos"** para o app usado (Arquivos/Chrome).
2. Abra o APK e instale.
3. Defina como tela inicial: **Configurações → Apps → Apps padrão → App de início → Aurora Launcher**.
   (ou pressione o botão Home e escolha "Aurora Launcher" → **Sempre**)

## Recursos

- Lista todos os apps instalados (precisa de `QUERY_ALL_PACKAGES` no Android 11+)
- Índice alfabético lateral com toque para pular
- Tema escuro (#07080C) com acento ciano
- Voltar não sai do launcher

## Estrutura

```
app/src/main/java/org/aurora/launcher/MainActivity.kt   → tela e lista de apps
app/src/main/java/org/aurora/launcher/AppAdapter.kt     → itens da lista
app/src/main/AndroidManifest.xml                        → filtro HOME (launcher)
.github/workflows/build.yml                             → compila o APK na nuvem
```

Licença: MIT.
