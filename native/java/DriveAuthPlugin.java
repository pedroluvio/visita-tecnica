package pt.luvio.visitatecnica;

import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.ClearTokenRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import java.io.File;
import java.util.Collections;
import java.util.Locale;

/**
 * Plugin nativo da APK:
 *  - authorize(): obtém um access token do Google Drive (escopo drive.file) através
 *    do AuthorizationClient do Google Play Services. Depois da primeira autorização
 *    renova o token sem qualquer interação (interactive = false).
 *  - clearToken(): limpa da cache um token que o Google rejeitou (401).
 *  - cleanupPhotos(): apaga fotografias temporárias antigas (originais da câmara).
 */
@CapacitorPlugin(name = "DriveAuth")
public class DriveAuthPlugin extends Plugin {

    private static final String DRIVE_FILE_SCOPE = "https://www.googleapis.com/auth/drive.file";

    private ActivityResultLauncher<IntentSenderRequest> authLauncher;
    private PluginCall pendingAuthCall;

    @Override
    public void load() {
        // Tem de ser registado enquanto a Activity ainda está a ser criada
        authLauncher = getActivity().registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            this::onAuthorizationResult
        );
    }

    @PluginMethod
    public void authorize(final PluginCall call) {
        if (pendingAuthCall != null) {
            call.reject("Já existe uma autorização em curso.", "busy");
            return;
        }
        final boolean interactive = Boolean.TRUE.equals(call.getBoolean("interactive", false));

        AuthorizationRequest request = AuthorizationRequest.builder()
            .setRequestedScopes(Collections.singletonList(new Scope(DRIVE_FILE_SCOPE)))
            .build();

        Identity.getAuthorizationClient(getActivity())
            .authorize(request)
            .addOnSuccessListener(result -> {
                if (result.hasResolution()) {
                    // Falta o consentimento do utilizador
                    if (!interactive) {
                        call.reject("É necessário autorizar o acesso ao Google Drive.", "needs_consent");
                        return;
                    }
                    try {
                        pendingAuthCall = call;
                        authLauncher.launch(
                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build()
                        );
                    } catch (Exception e) {
                        pendingAuthCall = null;
                        call.reject("Não foi possível abrir o ecrã de autorização.", "launch_failed", e);
                    }
                } else {
                    resolveWithToken(call, result);
                }
            })
            .addOnFailureListener(e -> {
                String code = "auth_failed";
                if (e instanceof ApiException) {
                    code = "api_" + ((ApiException) e).getStatusCode();
                }
                call.reject(e.getMessage() != null ? e.getMessage() : "Falha na autorização Google.", code, e);
            });
    }

    private void onAuthorizationResult(ActivityResult activityResult) {
        final PluginCall call = pendingAuthCall;
        pendingAuthCall = null;
        if (call == null) {
            return;
        }
        if (activityResult.getResultCode() != Activity.RESULT_OK) {
            call.reject("Autorização cancelada.", "cancelled");
            return;
        }
        try {
            Intent data = activityResult.getData();
            AuthorizationResult result = Identity.getAuthorizationClient(getActivity())
                .getAuthorizationResultFromIntent(data);
            resolveWithToken(call, result);
        } catch (ApiException e) {
            call.reject(
                e.getMessage() != null ? e.getMessage() : "Falha na autorização Google.",
                "api_" + e.getStatusCode(),
                e
            );
        }
    }

    private void resolveWithToken(PluginCall call, AuthorizationResult result) {
        String token = result.getAccessToken();
        if (token == null || token.isEmpty()) {
            call.reject("O Google não devolveu nenhum token de acesso.", "no_token");
            return;
        }
        JSObject ret = new JSObject();
        ret.put("accessToken", token);
        call.resolve(ret);
    }

    @PluginMethod
    public void clearToken(final PluginCall call) {
        String token = call.getString("accessToken");
        if (token == null || token.isEmpty()) {
            call.resolve();
            return;
        }
        Identity.getAuthorizationClient(getActivity())
            .clearToken(ClearTokenRequest.builder().setToken(token).build())
            .addOnSuccessListener(unused -> call.resolve())
            .addOnFailureListener(e -> call.resolve()); // falhar a limpar a cache não é crítico
    }

    @PluginMethod
    public void cleanupPhotos(final PluginCall call) {
        int hours = call.getInt("olderThanHours", 24);
        long cutoff = System.currentTimeMillis() - hours * 3600L * 1000L;
        int removed = 0;
        removed += deleteOldImages(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), cutoff);
        removed += deleteOldImages(getContext().getCacheDir(), cutoff);
        JSObject ret = new JSObject();
        ret.put("removed", removed);
        call.resolve(ret);
    }

    private int deleteOldImages(File dir, long cutoff) {
        if (dir == null || !dir.isDirectory()) {
            return 0;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return 0;
        }
        int count = 0;
        for (File f : files) {
            if (!f.isFile()) {
                continue;
            }
            String name = f.getName().toLowerCase(Locale.ROOT);
            boolean isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
            if (isImage && f.lastModified() < cutoff && f.delete()) {
                count++;
            }
        }
        return count;
    }
}
