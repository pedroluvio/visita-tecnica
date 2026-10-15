# Visita Técnica — APK

O mesmo `index.html` serve a PWA (GitHub Pages) e a APK. Dentro da APK o código deteta que está numa app e liga as partes nativas; tudo o resto (Drive, pastas, JSON, PDF, premissas) funciona exatamente como na PWA.

| | PWA | APK |
|---|---|---|
| Fotografia com a câmara | câmara do browser (a PWA pode reiniciar depois da 1.ª foto — usa a galeria) | câmara nativa; se o Android fechar a app a meio, a foto é recuperada ao reabrir |
| Login do Google Drive | redireciona para o Google, de hora a hora | login nativo; renova-se sozinho, sem ecrãs |
| PDF/JSON quando não há Drive | descarrega | abre a partilha do Android (guardar em Ficheiros, WhatsApp, e-mail…) |
| Botão "voltar" | do browser | fecha janelas abertas; dentro da visita volta ao ecrã inicial; no ecrã inicial pede um 2.º toque para sair |
| Resto | igual | igual |

## O que há de novo no `index.html` (PWA e APK)

- **Fotografias em cada item da checklist** (botão 📷 Foto, ao lado de 💬 Observações). Várias fotos por item; toca na miniatura para a ver (1.º toque mostra ×, 2.º abre). Uma foto com **!** não chegou ao Drive: toca nela para reenviar.
- **Drive por secção:** `ID - Morada - Visita técnica / Fotografias / 1 - Estrutura / 1.2 Tem fissuras estruturais graves - 20261014-101500-1.jpg` (a pasta `Fotografias` só é criada quando há a 1.ª foto).
- **PDF da checklist com as fotografias junto a cada item** (reduzidas a ~1100 px; a original fica no Drive). Se alguma foto não puder ser carregada, a app pergunta antes de gerar o PDF.
- **Retoma a visita a partir do Drive:** ao abrir a visita com o *mesmo ID + morada*, a app carrega o `dados.json` e as fotografias (visita iniciada na PWA, noutro telemóvel, ou depois de reinstalar).
- **Nunca mistura imóveis:** se mudares o ID/morada com uma visita em curso, a app pergunta se queres começar uma visita nova.
- O indicador **Drive** do cabeçalho também se toca (para ligar/religar sem voltar ao ecrã inicial).

---

## 1. Google Cloud (uma vez, ≈ 5 min)

1. Abre <https://console.developers.google.com/auth/clients> e escolhe o **mesmo projeto da PWA** (o do Client ID que começa por `95430853177-`) — o mesmo da APK da Vistoria. Tem de ser o mesmo, senão a APK pode não ver as pastas criadas pela PWA.
2. **Create client** (Criar cliente) → tipo **Android** →
   - Nome: `Visita Tecnica APK`
   - Nome do pacote: `pt.luvio.visitatecnica`
   - Impressão digital SHA-1: `73:5F:07:F5:88:68:59:00:38:79:DB:6C:09:EA:FD:AE:A2:14:F8:01`
   - **Create**. Não é preciso copiar nenhum ID para a app.
3. **Audience / Data Access:** já ficou feito para a Vistoria (projeto em *In production* e âmbito `drive.file`). Se não, passa de *Testing* para *In production* (em *Testing* a autorização caduca de 7 em 7 dias).

> A APK da Visita Técnica é uma app **separada** da da Vistoria (pacote e chave de assinatura próprios). Podes ter as duas instaladas.

## 2. GitHub (uma vez)

1. No repositório da PWA (`visita-tecnica`), carrega para a **raiz** todo o conteúdo desta pasta, substituindo o `index.html` e o `README.md`:
   `index.html`, `package.json`, `package-lock.json`, `capacitor.config.json`, `.gitignore`, `README.md`, `LEIA-ME-APK.md` e as pastas `native/`, `scripts/` e `.github/`.
   Confirma que existe `.github/workflows/build-apk.yml` (pastas que começam por ponto ficam ocultas em alguns explorers).
   O novo `index.html` continua a funcionar na PWA como antes (e ganha as fotografias).
2. **Settings → Secrets and variables → Actions → New repository secret.** Cria os 4 segredos com os valores do ficheiro `SEGREDOS-GITHUB.txt`:
   `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. (São segredos **deste repositório**: os da Vistoria não servem aqui.)
3. Separador **Actions → Compilar APK → Run workflow.** A 1.ª compilação demora ≈ 10 min; as seguintes ≈ 4 min. Daí em diante compila sozinho sempre que alteras o `index.html`.
4. No fim, abre a execução: o resumo mostra o *SHA-1 do certificado* — tem de ser igual ao registado no passo 1 (`73:5F:07:F5:88:68:59:00:38:79:DB:6C:09:EA:FD:AE:A2:14:F8:01`).

## 3. Instalar no telemóvel

1. No repositório → **Releases → "APK mais recente"** → descarrega `Visita-Tecnica.apk`.
2. Abre o ficheiro. Se o Android pedir, autoriza "instalar apps desconhecidas" para o browser/Ficheiros. Se o Play Protect avisar, escolhe *Mais detalhes → Instalar mesmo assim*.
3. Abre a app → toca no indicador **Drive** → escolhe a **mesma conta Google da PWA** → autoriza. A partir daqui liga-se sozinha de cada vez.
4. Atualizações: instala o APK novo por cima do antigo (mantém os dados).

## 4. Testes a fazer (≈ 10 min)

1. **Drive:** o indicador fica "Drive ✓". Fecha a app e reabre: deve ficar ligado sem tocares em nada.
2. **Continuidade com a PWA:** abre a visita com o *mesmo ID + morada* de uma já feita na PWA. Deve aparecer "Sessão carregada do Google Drive". Se a pasta for nova ("Pasta … criada"), a APK não está a ver as pastas da PWA (ver "Se algo correr mal").
3. **Câmara por item:** num item toca em **📷 Foto → Tirar fotografia**; tira 4–5 seguidas; cada uma mostra o círculo a rodar e fica fixa quando chega ao Drive.
4. **Pastas:** no Drive, a foto tem de estar em `… - Visita técnica / Fotografias / 1 - Estrutura /` (a pasta da secção do item).
5. **Recuperação:** Definições do Android → Opções de programador → ligar **"Não manter atividades"**. Tira uma fotografia: ao voltar da câmara a app reabre sozinha na checklist e mostra "Fotografia recuperada…". Volta a desligar a opção.
6. **PDF:** com Drive ligado → "PDF guardado no Google Drive" (as fotos aparecem junto a cada item). Em modo avião → abre a partilha do Android.
7. **Voltar:** com uma janela aberta fecha-a; dentro da visita volta ao início; no início, 2 toques seguidos para sair.

## Se algo correr mal

- **"Google Cloud: o cliente Android (pacote/SHA-1) não corresponde…" (código `api_10`):** o cliente Android não existe, está noutro projeto, ou o SHA-1/pacote não é o desta APK (`pt.luvio.visitatecnica` + `73:5F:07:F5:88:68:59:00:38:79:DB:6C:09:EA:FD:AE:A2:14:F8:01`).
- **Pasta nova quando devia existir:** o Google só mostra a uma app os ficheiros criados por ela (ou por outro cliente do mesmo projeto). Confirma que o cliente Android foi criado no mesmo projeto da PWA.
- **A compilação fica vermelha no Actions:** abre o passo que falhou, copia as últimas ~40 linhas do registo e envia-mas.
- **Trocar de conta Google:** Definições do Android → Apps → Visita Técnica → Armazenamento → Limpar dados.
- **Fotografias temporárias:** a app apaga sozinha as da câmara com mais de 24 h (já estão no Drive).
- **Apagar uma foto na app** (×) também a apaga do Drive (definitivamente, sem passar pelo lixo).

## Notas técnicas

- Capacitor 8.5.3 · Java 21 · Node 22 · AGP 8.13 · pacote `pt.luvio.visitatecnica` · minSdk 24 (Android 7+).
- A pasta `android/` **não** está no repositório: o Actions gera-a de cada vez (`npm run android:prepare`) a partir do `index.html`, dos plugins e de `native/` (plugin Java do login Google, ícones). Para mudar o ícone, edita `scripts/make-icons.py` e corre-o.
- No Drive a APK usa o âmbito `drive.file`, tal como a PWA. Na APK os pedidos ao Drive não levam a chave de API (o token OAuth basta).
- **Chave de assinatura:** o `SEGREDOS-GITHUB.txt` contém a chave completa. Guarda-o num sítio seguro e nunca o ponhas no GitHub. Se o perderes não consegues atualizar por cima: terias de desinstalar a app e registar um SHA-1 novo no Google Cloud (os dados no Drive não se perdem).
