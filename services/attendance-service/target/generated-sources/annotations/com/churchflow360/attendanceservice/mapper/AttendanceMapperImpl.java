package com.churchflow360.attendanceservice.mapper;

import com.churchflow360.attendanceservice.dto.request.CreateServiceEventRequest;
import com.churchflow360.attendanceservice.dto.response.AttendanceRecordResponse;
import com.churchflow360.attendanceservice.dto.response.ServiceEventResponse;
import com.churchflow360.attendanceservice.model.AttendanceRecord;
import com.churchflow360.attendanceservice.model.ServiceEvent;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-28T22:51:56-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class AttendanceMapperImpl implements AttendanceMapper {

    @Override
    public ServiceEvent toEntity(CreateServiceEventRequest request) {
        if ( request == null ) {
            return null;
        }

        ServiceEvent.ServiceEventBuilder serviceEvent = ServiceEvent.builder();

        serviceEvent.title( request.getTitle() );
        serviceEvent.serviceType( request.getServiceType() );
        serviceEvent.serviceDate( request.getServiceDate() );
        serviceEvent.startTime( request.getStartTime() );
        serviceEvent.endTime( request.getEndTime() );
        serviceEvent.location( request.getLocation() );
        serviceEvent.description( request.getDescription() );

        return serviceEvent.build();
    }

    @Override
    public ServiceEventResponse toResponse(ServiceEvent event) {
        if ( event == null ) {
            return null;
        }

        ServiceEventResponse serviceEventResponse = new ServiceEventResponse();

        serviceEventResponse.setId( event.getId() );
        serviceEventResponse.setTitle( event.getTitle() );
        serviceEventResponse.setServiceType( event.getServiceType() );
        serviceEventResponse.setServiceDate( event.getServiceDate() );
        serviceEventResponse.setStartTime( event.getStartTime() );
        serviceEventResponse.setEndTime( event.getEndTime() );
        serviceEventResponse.setLocation( event.getLocation() );
        serviceEventResponse.setDescription( event.getDescription() );
        serviceEventResponse.setQrToken( event.getQrToken() );
        serviceEventResponse.setQrExpiresAt( event.getQrExpiresAt() );
        serviceEventResponse.setCreatedAt( event.getCreatedAt() );

        serviceEventResponse.setQrValid( event.isQrValid() );
        serviceEventResponse.setTotalAttendance( event.getTotalAttendance() );

        return serviceEventResponse;
    }

    @Override
    public AttendanceRecordResponse toResponse(AttendanceRecord record) {
        if ( record == null ) {
            return null;
        }

        AttendanceRecordResponse attendanceRecordResponse = new AttendanceRecordResponse();

        attendanceRecordResponse.setServiceEventId( recordServiceEventId( record ) );
        attendanceRecordResponse.setServiceEventTitle( recordServiceEventTitle( record ) );
        attendanceRecordResponse.setId( record.getId() );
        attendanceRecordResponse.setMembershipNumber( record.getMembershipNumber() );
        attendanceRecordResponse.setMemberName( record.getMemberName() );
        attendanceRecordResponse.setCheckinMethod( record.getCheckinMethod() );
        attendanceRecordResponse.setCheckedInAt( record.getCheckedInAt() );
        attendanceRecordResponse.setNotes( record.getNotes() );

        return attendanceRecordResponse;
    }

    private UUID recordServiceEventId(AttendanceRecord attendanceRecord) {
        if ( attendanceRecord == null ) {
            return null;
        }
        ServiceEvent serviceEvent = attendanceRecord.getServiceEvent();
        if ( serviceEvent == null ) {
            return null;
        }
        UUID id = serviceEvent.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String recordServiceEventTitle(AttendanceRecord attendanceRecord) {
        if ( attendanceRecord == null ) {
            return null;
        }
        ServiceEvent serviceEvent = attendanceRecord.getServiceEvent();
        if ( serviceEvent == null ) {
            return null;
        }
        String title = serviceEvent.getTitle();
        if ( title == null ) {
            return null;
        }
        return title;
    }
}
