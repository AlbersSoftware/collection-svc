package com.kinorify.collection.entity.enums;

public enum CollectionRelationshipRole {

    VIEW_ONLY(1),
    CAN_ADD(2),
    OWNER(3);

    private final int priority;

    CollectionRelationshipRole(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }
}
