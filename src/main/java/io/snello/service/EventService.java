package io.snello.service;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import io.snello.model.Condition;
import io.snello.model.Draggable;
import io.snello.model.Droppable;
import io.snello.model.SelectQuery;
import io.snello.model.events.*;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class EventService {
    
    @Inject
    MetadataService metadataService;

    public void onLoad(@Observes StartupEvent event) {
        Log.info("******** START METADATA SERVICE AT STARTUP ******** ");
        try {
            resetAll();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
        Log.info("******** END METADATA SERVICE AT STARTUP ********");
    }

    public void resetAll() throws Exception {
        // null to maps
        metadataService.conditionsMap = null;
        metadataService.selectqueryMap = null;
        // reset
        metadataService.conditionsMap();
        metadataService.selectqueryMap();
    }

    public void resetMetadata() throws Exception {
        metadataService.metadataMap = null;
        metadataService.fielddefinitionsMap = null;
        metadataService.metadataMap();
        metadataService.fielddefinitionsMap();
    }

    void createOrUpdateMetadata(@ObservesAsync MetadataCreateUpdateEvent metadataCreateUpdateEvent) {
        Log.info("new MetadataCreateUpdateEvent " + metadataCreateUpdateEvent.toString());
        try {
            resetMetadata();
//            metadataService.metadataMap().put(metadataCreateUpdateEvent.metadata.table_name, metadataCreateUpdateEvent.metadata);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    void deleteMetadata(@ObservesAsync MetadataDeleteEvent metadataDeleteEvent) {
        Log.info("new MetadataDeleteEvent " + metadataDeleteEvent.toString());
        try {
            resetMetadata();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


    void createOrUpdateFieldDefinition(@ObservesAsync FieldDefinitionCreateUpdateEvent fieldDefinitionCreateUpdateEvent) {
        Log.info("new FieldDefinitionCreateUpdateEvent " + fieldDefinitionCreateUpdateEvent.toString());
//        Map<String, FieldDefinition> fieldDefinitions = null;
//        try {
//            if (metadataService.fielddefinitionsMap().containsKey(fieldDefinitionCreateUpdateEvent.fieldDefinition.metadata_name)) {
//                fieldDefinitions = metadataService.fielddefinitionsMap().get(fieldDefinitionCreateUpdateEvent.fieldDefinition.metadata_name);
//            } else {
//                fieldDefinitions = new HashMap<>();
//            }
//        } catch (Exception e) {
//            Log.error(e.getMessage(), e);
//        }
////        if (!fieldDefinitions.containsKey(fieldDefinitionCreateUpdateEvent.fieldDefinition.uuid)) {
////            fieldDefinitions.put(fieldDefinitionCreateUpdateEvent.fieldDefinition.uuid, fieldDefinitionCreateUpdateEvent.fieldDefinition);
////        }
//        fieldDefinitions.put(fieldDefinitionCreateUpdateEvent.fieldDefinition.uuid, fieldDefinitionCreateUpdateEvent.fieldDefinition);
//        try {
//            metadataService.fielddefinitionsMap().put(fieldDefinitionCreateUpdateEvent.fieldDefinition.metadata_name, fieldDefinitions);
//        } catch (Exception e) {
//            Log.error(e.getMessage(), e);
//        }
        try {
            resetMetadata();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


    void deleteFieldDefinition(@ObservesAsync FieldDefinitionDeleteEvent fieldDefinitionDeleteEvent) {
        Log.info("new FieldDefinitionDeleteEvent " + fieldDefinitionDeleteEvent.toString());
//        try {
//            for (Map<String, FieldDefinition> fieldDefinitions : metadataService.fielddefinitionsMap().values()) {
//                for (FieldDefinition fieldDefinition : fieldDefinitions.values()) {
//                    if (fieldDefinition.uuid.equals(fieldDefinitionDeleteEvent.uuid)) {
//                        fieldDefinitions.remove(fieldDefinition.uuid);
//                        metadataService.fielddefinitionsMap().put(fieldDefinition.metadata_name, fieldDefinitions);
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.error(e.getMessage(), e);
//        }
        try {
            resetMetadata();
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    void createOrUpdateDraggable(@ObservesAsync DraggableCreateUpdateEvent draggableCreateUpdateEvent) {
        Log.info("new DraggableCreateUpdateEvent " + draggableCreateUpdateEvent.toString());
        try {
            metadataService.draggablesMap().put(draggableCreateUpdateEvent.draggable.uuid, draggableCreateUpdateEvent.draggable);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


    void deleteDraggable(@ObservesAsync DraggableDeleteEvent draggableDeleteEvent) {
        Log.info("new DraggableDeleteEvent " + draggableDeleteEvent.toString());
        try {
            for (Draggable draggable : metadataService.draggablesMap().values()) {
                if (draggable.uuid.equals(draggableDeleteEvent.uuid)) {
                    metadataService.draggablesMap().remove(draggable.uuid);
                    break;
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    void createOrUpdateDroppable(@ObservesAsync DroppableCreateUpdateEvent droppableCreateUpdateEvent) {
        Log.info("new DroppableCreateUpdateEvent " + droppableCreateUpdateEvent.toString());
        try {
            metadataService.droppablesMap().put(droppableCreateUpdateEvent.droppable.uuid, droppableCreateUpdateEvent.droppable);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    void deleteDroppable(@ObservesAsync DroppableDeleteEvent droppableDeleteEvent) {
        Log.info("new DroppableDeleteEvent " + droppableDeleteEvent.toString());
        try {
            for (Droppable droppable : metadataService.droppablesMap().values()) {
                if (droppable.uuid.equals(droppableDeleteEvent.uuid)) {
                    metadataService.droppablesMap().remove(droppable.uuid);
                    break;
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    void createOrUpdateSelectQuery(@ObservesAsync SelectQueryCreateUpdateEvent selectQueryCreateUpdateEvent) {
        Log.info("new SelectQueryCreateUpdateEvent " + selectQueryCreateUpdateEvent.toString());
        try {
            metadataService.selectqueryMap().put(selectQueryCreateUpdateEvent.selectQuery.query_name, selectQueryCreateUpdateEvent.selectQuery);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    void deleteSelectQuery(@ObservesAsync SelectQueryDeleteEvent selectQueryDeleteEvent) {
        Log.info("new SelectQueryDeleteEvent " + selectQueryDeleteEvent.toString());
        try {
            for (SelectQuery selectQuery : metadataService.selectqueryMap().values()) {
                if (selectQuery.uuid.equals(selectQueryDeleteEvent.uuid)) {
                    metadataService.selectqueryMap().remove(selectQuery.query_name);
                    break;
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


    void createOrUpdateCondition(@ObservesAsync ConditionCreateUpdateEvent conditionCreateUpdateEvent) {
        Log.info("new ConditionCreateUpdateEvent " + conditionCreateUpdateEvent.toString());
        List<Condition> conditions = null;
        try {
            if (metadataService.conditionsMap().containsKey(conditionCreateUpdateEvent.condition.metadata_name)) {
                conditions = metadataService.conditionsMap().get(conditionCreateUpdateEvent.condition.metadata_name);
            } else {
                conditions = new ArrayList<>();
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
        if (!conditions.contains(conditionCreateUpdateEvent.condition)) {
            conditions.add(conditionCreateUpdateEvent.condition);
        }
        try {
            metadataService.conditionsMap().put(conditionCreateUpdateEvent.condition.metadata_name, conditions);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


    void deleteCondition(@ObservesAsync ConditionDeleteEvent conditionDeleteEvent) {
        Log.info("new ConditionDeleteEvent " + conditionDeleteEvent.toString());
        try {
            for (List<Condition> conditions : metadataService.conditionsMap().values()) {
                for (Condition condition : conditions) {
                    if (condition.uuid.equals(conditionDeleteEvent.uuid)) {
                        conditions.remove(condition);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }


}
