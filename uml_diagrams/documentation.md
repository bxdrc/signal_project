# UML Class Diagrams — CHMS Documentation

## 1. Alert Generation System

The Alert Generation System is designed around a clear separation of concerns between evaluating data, representing alerts, and dispatching them to staff. The central class, `AlertGenerator`, is responsible for continuously evaluating incoming patient data against a set of configurable `AlertThreshold` objects. Each threshold encapsulates the logic for a single rule — for example, "heart rate above 130 bpm for Patient A" — and exposes an `isExceeded()` method that `AlertGenerator` calls per data point. This avoids hard-coding alert logic inside the evaluator, making it easy to add or modify thresholds without changing core evaluation logic.

When a threshold is exceeded, `AlertGenerator` creates an `Alert` object containing the patient ID, the violated condition, a timestamp, and a severity level. This object is then passed to `AlertManager`, which maintains the list of active alerts and is responsible for notifying medical staff via `StaffNotifier`. Separating dispatch from evaluation means the notification mechanism (email, pager, on-screen alert) can be changed independently of how alerts are generated.

`DataStorage` is referenced as an external dependency, reflecting that the alert system reads patient data but does not own it. This keeps the subsystem's responsibilities focused and avoids coupling between storage and alerting logic. The design is open for extension — new alert types or escalation policies can be added by extending `AlertManager` or creating new `AlertThreshold` subclasses.

---

## 2. Data Storage System

The Data Storage System is designed with security, modularity, and data lifecycle management as its core concerns. `DataStorage` acts as the central repository, maintaining a map of patient IDs to their respective lists of `PatientRecord` objects. Each record is immutable after creation, capturing a single timestamped measurement, which supports both real-time monitoring and historical trend analysis.

Access to stored data is mediated by `AccessController`, which enforces role-based access control. This ensures that only authorized roles (e.g., doctors, nurses) can retrieve patient data, directly addressing the privacy requirements of a hospital environment. `DataRetriever` acts as the query layer, providing structured methods for staff to retrieve records by patient, time range, or measurement type, rather than accessing storage directly.

The `DataDeletionPolicy` class manages data lifecycle by removing records older than a configurable number of days. This is important in a real hospital setting where indefinite data retention may violate privacy regulations. Finally, `DataAuditLog` records all access and deletion events, ensuring accountability and traceability — critical properties in safety-critical systems. The overall design follows the principle of least privilege: each class only interacts with storage in ways appropriate to its role.

---

## 3. Patient Identification System

The Patient Identification System is responsible for reliably linking incoming simulator data to the correct hospital patient. The central class, `PatientIdentifier`, serves as the entry point for all matching requests. It delegates lookup operations to `IdentityManager`, which maintains the authoritative list of registered `HospitalPatient` objects and the mappings between simulator IDs and real hospital patient IDs.

`HospitalPatient` encapsulates all patient-specific information, including medical history and ward assignment. Its fields are private and only accessible through getters, ensuring sensitive data is not inadvertently exposed to other subsystems. `IdentityManager` queries an external `HospitalDatabase` (modeled as an external reference) to retrieve or update patient records, keeping the system's own state synchronized with the hospital's central data source.

A key design decision is the explicit handling of mismatches. When `PatientIdentifier` cannot match an incoming simulator ID, it logs the event to `MismatchLog`, which stores structured `MismatchEntry` objects. This makes anomalies visible and auditable rather than silently ignored — an important property in a clinical setting where unmatched data could represent a system error or a patient safety risk. The clear boundary between `PatientIdentifier` (orchestration) and `IdentityManager` (data management) keeps each class focused and testable.

---

## 4. Data Access Layer

The Data Access Layer is designed to abstract the underlying data source from the rest of the CHMS. The system can receive data from three external sources — TCP sockets, WebSocket connections, and log files — and the layer ensures the rest of the system remains unaware of which source is in use.

This is achieved through an abstract base class, `DataListener`, which defines the common interface (`startListening()`, `stopListening()`) for all input types. The three concrete subclasses — `TCPDataListener`, `WebSocketDataListener`, and `FileDataListener` — each implement this interface for their respective protocol or format. Adding a new data source in the future requires only creating a new subclass of `DataListener`, with no changes to the rest of the system.

Once raw data is received, it is handed to `DataParser`, which normalizes it into a standardized `PatientRecord` object regardless of the original format (JSON, CSV, or plain text). `DataSourceAdapter` acts as the bridge between the listener layer and the storage layer, calling `DataParser` to convert incoming data and then forwarding the result to `DataStorage`. This separation ensures that parsing logic, listening logic, and storage logic each live in their own class, making the system easier to test, modify, and extend. The adapter pattern used here also means the external data sources are fully decoupled from the internal data model.
