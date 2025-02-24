package com.example.appointmentmanagement.repository;

import com.example.appointmentmanagement.model.Service;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for managing service data in Firestore.
 */
public class ServiceRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore database instance

    /**
     * Callback interface for service operations.
     */
    public interface ServiceCallback {
        void onSuccess(List<Service> services);
        void onFailure(Exception e);
    }

    /**
     * Retrieves all services from Firestore.
     *
     * @param callback Callback function to return the list of services.
     */
    public void getAllServices(ServiceCallback callback) {
        db.collection("services").get().addOnSuccessListener(result -> {
            List<Service> services = new ArrayList<>();
            for (QueryDocumentSnapshot doc : result) {
                services.add(new Service(doc.getId(), doc.getString("name")));
            }
            callback.onSuccess(services);
        }).addOnFailureListener(callback::onFailure);
    }

    /**
     * Adds a new service to Firestore.
     *
     * @param name     The name of the service to be added.
     * @param callback Callback function executed on success.
     */
    public void addService(String name, Runnable callback) {
        db.collection("services").add(new Service(null, name))
                .addOnSuccessListener(documentReference -> callback.run())
                .addOnFailureListener(Throwable::printStackTrace);
    }

    /**
     * Updates the name of an existing service in Firestore.
     *
     * @param id       The ID of the service to be updated.
     * @param newName  The new name for the service.
     * @param callback Callback function executed on success.
     */
    public void updateService(String id, String newName, Runnable callback) {
        db.collection("services").document(id).update("name", newName)
                .addOnSuccessListener(aVoid -> callback.run())
                .addOnFailureListener(Throwable::printStackTrace);
    }

    /**
     * Deletes a service from Firestore.
     *
     * @param id       The ID of the service to be deleted.
     * @param callback Callback function executed on success.
     */
    public void deleteService(String id, Runnable callback) {
        db.collection("services").document(id).delete()
                .addOnSuccessListener(aVoid -> callback.run())
                .addOnFailureListener(Throwable::printStackTrace);
    }
}
