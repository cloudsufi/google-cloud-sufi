package com.google.adapter.outbound;

/**
 *
 */
public interface SapSubscriber {
    void onIDocChange(String idocType, String idoc, String payload, String release);
}
