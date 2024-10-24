package com.meteor.SBPractice.Database;

import java.util.UUID;

public interface Database {
    void initialize();

    void setDestructions(UUID key, int value);

    int getDestructions(UUID key);

    void setPlacements(UUID key, int value);

    int getPlacements(UUID key);

    void setRestores(UUID key, int value);

    int getRestores(UUID key);
}
