package io.snello.service.rs.security;

import io.snello.management.AppConstants;
import io.snello.service.ApiService;
import io.snello.service.mail.Email;
import io.snello.service.mail.EmailService;
import io.snello.util.PasswordUtils;
import io.snello.util.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.snello.management.AppConstants.*;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.serverError;

@Path(PASSWORD_PATH)
public class ChangePasswordController {

    Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);


    @Inject
    ApiService apiService;

    @Inject
    EmailService emailService;

    String table = AppConstants.USERS;
    String UUID = AppConstants.USERNAME;

    @POST
    @Path(UUID_PATH_PARAM_RESET)
    public Response reset(@NotNull String uuid) throws Exception {
        Map<String, Object> map = apiService.fetch(null, table, uuid, UUID);
        if (map != null && map.containsKey(EMAIL)) {
            //GENERO UN TOKEN E LO INVIO TRAMITE EMAIL
            String token = RandomUtils.aphaNumericString(6);
            //DEVO AVERE UNA PAGINA DI CAMBIO PASSWORD
            String mail = (String) map.get(EMAIL);
            String subject = "reset password SNELLO CMS";
            String body = "The token to change your password is: " + token;
            Email emailObj = new Email(mail, subject, body);
            emailService.send(emailObj);

            Map<String, Object> changePasswordTokenMap = new HashMap<>();
            changePasswordTokenMap.put(AppConstants.UUID, java.util.UUID.randomUUID().toString());
            changePasswordTokenMap.put(EMAIL, uuid);
            changePasswordTokenMap.put(TOKEN, token);
            changePasswordTokenMap.put(CREATION_DATE, Instant.now().toString());
            changePasswordTokenMap = apiService.create(CHANGE_PASSWORD_TOKENS, changePasswordTokenMap, UUID);
            return ok(changePasswordTokenMap).build();
        }
        return serverError().build();
    }


    @POST
    @Path(UUID_PATH_PARAM_CHANGE)
    public Response change( Map<String, Object> mapVerify, @NotNull String uuid) throws Exception {
        Map<String, Object> tokenMap = apiService.fetch(null, CHANGE_PASSWORD_TOKENS,
                (String) mapVerify.get(TOKEN), TOKEN);
        boolean tokenValido = false;
        boolean pwdValida = false;
        if (tokenMap != null && tokenMap.containsKey(EMAIL)
                && tokenMap.get(EMAIL).equals(uuid)
        ) {
            tokenValido = true;
        } else {
            logger.info("no token Valido: " + mapVerify.get(TOKEN) + " - for uuid: " + uuid);
        }
        if (tokenMap != null && mapVerify.containsKey(PASSWORD)
                && mapVerify.containsKey(CONFIRM_PASSWORD)
                && mapVerify.get(PASSWORD).equals(mapVerify.get(CONFIRM_PASSWORD))
        ) {
            pwdValida = true;
        } else {
            logger.info("no pwd valida: " + mapVerify.get(TOKEN) + " - for uuid: " + uuid);
        }
        if (tokenValido && pwdValida) {
            Map<String, Object> map = new HashMap<>();
            String pwd = PasswordUtils.createPassword((String) mapVerify.get(AppConstants.PASSWORD));
            map.put(AppConstants.PASSWORD, pwd);
            map.put(AppConstants.LAST_UPDATE_DATE, Instant.now().toString());
            map = apiService.merge(table, map, uuid, UUID);
            return ok(map).build();
        } else {
            logger.info("!!PAY ATTENTION!! tokenValido: " + tokenValido + " or pwdValida: " + pwdValida);
        }
        return serverError().build();
    }
}
