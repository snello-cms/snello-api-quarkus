package io.snello.filter;

import io.quarkus.logging.Log;
import io.snello.service.SystemLogService;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.StringJoiner;


@Provider
@PreMatching
public class LogFilter implements ContainerRequestFilter {

    @Inject
    SystemLogService systemEventLogService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String principal = "system";
        if (requestContext.getHeaderString("accept") == null
                || requestContext.getEntityStream() == null
                || !requestContext.getHeaderString("accept").contains("application/json")
                || requestContext.getUriInfo().getPath().contains("/api/v1/attachments")
                || requestContext.getUriInfo().getPath().contains("/api/v1/barcode")
                || requestContext.getMethod().equals("GET")
                || requestContext.getUriInfo().getPath().contains("deviceevents")) {
            return;
        } else {
            Log.info("LogFilter: getMediaType " + requestContext.getHeaderString("accept"));
        }
        byte[] bytes = null;
        switch (requestContext.getMethod()) {
            case "POST":
            case "PUT":
            case "DELETE":
                Log.info("LogFilter: method " + requestContext.getRequest().getMethod());
                try {
                    StringJoiner sj = new StringJoiner(",");
                    if (requestContext.getSecurityContext() != null
                            && requestContext.getSecurityContext().getUserPrincipal() != null
                            && requestContext.getSecurityContext().getUserPrincipal().getName() != null
                    ) {
                        principal = requestContext.getSecurityContext().getUserPrincipal().getName();
                    }
                    String method = requestContext.getMethod();
                    int initialOffset = "/api/v1/".length(); // length of `/v1/`
                    String path = requestContext.getUriInfo().getPath();
                    sj.add("path: " + path);
                    String newPath = path.substring(initialOffset);
                    if (newPath.contains("/") || newPath.contains("?")) {
                        String[] split = newPath.split("/|\\?");
                        String prefix = split[0];
                        newPath = prefix;
                    }
                    sj.add("principal: " + principal);
                    sj.add("method: " + method);
                    sj.add("entity: " + newPath);
                    sj.add("path params: " + requestContext.getUriInfo().getPathParameters());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    if (requestContext != null && requestContext.getEntityStream() != null) {
                        bytes = requestContext.getEntityStream().readAllBytes();
                        String jsonObject = new String(bytes, "UTF-8");
                        systemEventLogService.persistSystemLog(newPath, method, jsonObject, principal);
                    } else {
                        Log.info("LogFilter: entity vuoto??");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                requestContext.setEntityStream(new ByteArrayInputStream(bytes));
                break;
            default:
                break;
        }

    }

}
//@Provider
//@PreMatching
//public class LogFilter implements ContainerRequestFilter
//{
//
//   Logger logger = Log.getLogger(getClass());
//
//   @Inject SystemLogService systemLogService;
//
//   @Override
//   public void filter(ContainerRequestContext requestContext) throws IOException
//   {
//      String principal = "system";
//      if (!MediaType.APPLICATION_JSON_TYPE.equals(requestContext.getMediaType())
//               || requestContext.getEntityStream() == null)
//      {
//         return;
//      }
//      if (requestContext.getHeaderString("Authorization") != null && !requestContext.getHeaderString("Authorization")
//               .isEmpty())
//      {
//         String token = requestContext.getHeaderString("Authorization").replace("Bearer ", "").trim();
//         //con keycloak
//         //            try {
//         //                Map<String, Object> mappa = JWTUtils.decode(token, AppProperties.jwtSecret.value());
//         //                principal = mappa.get(USERNAME) != null ? mappa.get(USERNAME).toString() : "system";
//         //            } catch (SignatureException e) {
//         //                e.printStackTrace();
//         //            } catch (NoSuchAlgorithmException e) {
//         //                e.printStackTrace();
//         //            } catch (JWTVerifyException e) {
//         //                e.printStackTrace();
//         //            } catch (InvalidKeyException e) {
//         //                e.printStackTrace();
//         //            }
//      }
//      byte[] bytes = null;
//      switch (requestContext.getMethod())
//      {
//      case "POST":
//      case "PUT":
//      case "DELETE":
//         try
//         {
//            StringJoiner sj = new StringJoiner(",");
//            if (requestContext.getSecurityContext() != null
//                     && requestContext.getSecurityContext().getUserPrincipal() != null)
//            {
//               principal = requestContext.getSecurityContext().getUserPrincipal().getName();
//            }
//            String method = requestContext.getMethod();
//            int initialOffset = "/v1/".length(); // length of `/v1/`
//            String path = requestContext.getUriInfo().getPath();
//            sj.add("path: " + path);
//            String newPath = null;
//            if (path.equalsIgnoreCase("/login"))
//            {
//               newPath = path.substring(1);
//            }
//            else
//            {
//               newPath = path.substring(initialOffset);
//               Log.info(path.substring(initialOffset));
//               if (newPath.contains("/") || newPath.contains("?"))
//               {
//                  String[] split = newPath.split("/|\\?");
//                  String prefix = split[0];
//                  newPath = prefix;
//               }
//            }
//            sj.add("principal: " + principal);
//            sj.add("method: " + method);
//            sj.add("entity: " + newPath);
//            sj.add("path params: " + requestContext.getUriInfo().getPathParameters());
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bytes = requestContext.getEntityStream().readAllBytes();
//            String jsonObject = new String(bytes, "UTF-8");
//            if (newPath.equalsIgnoreCase("events"))
//            {
//               Log.info(sj.toString() + ", entity events: SKIP LOGGING: " + jsonObject);
//               requestContext.setEntityStream(new ByteArrayInputStream(bytes));
//               return;
//            }
//            if (newPath.equalsIgnoreCase("login") || newPath.equalsIgnoreCase("mobile"))
//            {
//               //{"username":"admin","password":"admin"}
//               jsonObject = jsonObject.substring(0, jsonObject.indexOf("password") + 9) + ": \"xxxxxxx\"}";
//            }
//            else
//            {
//
//            }
//            Log.info(sj.toString() + ", Posted: " + jsonObject);
//            //String obj, String operation_type, String data, String principal
//
//            systemLogService.persistSystemLog(newPath, method, jsonObject, principal);
//         }
//         catch (Exception e)
//         {
//            e.printStackTrace();
//         }
//         requestContext.setEntityStream(new ByteArrayInputStream(bytes));
//         break;
//      default:
//         break;
//      }
//
//   }
//
//}
