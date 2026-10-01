package dev.protoskey.websocket;

import dev.protoskey.event.CityChanged;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;

@ApplicationScoped
public class CityChangesBroadcaster {
    public void onCityChanged(@Observes(during = TransactionPhase.AFTER_SUCCESS) CityChanged event) {
        CityChangesEndpoint.broadcast("changed");
    }
}
