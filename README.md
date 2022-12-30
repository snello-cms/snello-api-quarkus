# snello-api-quarkus project

This Headless CMS uses Quarkus, the Supersonic Subatomic Java Framework. In the past we used Micronaut.

## TODO list 

### roles usable in keycloak

metadatas_edit, metadatas_view,fielddefinitions_edit, fielddefinitions_view,conditions_edit, conditions_view,documents_edit, documents_view,publicdata_edit,selectqueries_edit, selectqueries_view,users_edit, users_view,roles_edit,roles_view,urlmaprules_edit, urlmaprules_view,links_edit, links_view,draggables_edit, draggables_view,droppables_edit, droppables_view, extensions_edit, extensions_view'

### FOR ADMIN FRONTEND FUTURES
- rewrite all field definitions in formly format
- generate dinamically formly json definitions
  
### BACKEND FUTURES
- automatic generation of images in different formats DONE
- automatic webhooks after persist/update/delete
- export/import of data and documents in zip files
- logging of data changes on database

### github actions
- split the actions in different task
- maven build send packages to github repo
- maven task and save all target folder in some place to reuse in the generation of docker images
- 
