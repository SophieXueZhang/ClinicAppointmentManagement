
CREATE PROCEDURE AddAppointment
    @PatientID INT,
    @AppointmentDate DATETIME,
    @ReasonForVisit NVARCHAR(255),
    @AppointmentStatus NVARCHAR(50)
AS
BEGIN
    INSERT INTO Appointments (PatientID, AppointmentDate, ReasonForVisit, AppointmentStatus)
    VALUES (@PatientID, @AppointmentDate, @ReasonForVisit, @AppointmentStatus)
END
