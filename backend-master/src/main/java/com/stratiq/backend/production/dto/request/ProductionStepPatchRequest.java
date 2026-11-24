package com.stratiq.backend.production.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.stratiq.backend.production.model.ProductionStepStatus;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ProductionStepPatchRequest {

    private ProductionStepStatus status;

    private UUID assignedTo;
    // Tracks whether "assignedTo" key was present in JSON (null = remove assignment).
    private boolean assignedToProvided = false;

    // Required when this patch completes the LAST step of an order line: the finished good's
    // stock-IN target warehouse. No implicit "primary warehouse" concept exists, so the caller
    // completing the step must say where the produced asset physically ends up.
    private UUID warehouseId;

    public void setStatus(ProductionStepStatus status) {
        this.status = status;
    }

    public void setWarehouseId(UUID warehouseId) {
        this.warehouseId = warehouseId;
    }

    @JsonSetter("assignedTo")
    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
        this.assignedToProvided = true;
    }
}
