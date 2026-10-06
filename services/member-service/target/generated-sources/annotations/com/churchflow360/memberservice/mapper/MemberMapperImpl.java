package com.churchflow360.memberservice.mapper;

import com.churchflow360.memberservice.dto.request.MemberRequest;
import com.churchflow360.memberservice.dto.request.UpdateMemberRequest;
import com.churchflow360.memberservice.dto.response.MemberResponse;
import com.churchflow360.memberservice.model.Member;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-29T15:22:23-0400",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.9 (Oracle Corporation)"
)
@Component
public class MemberMapperImpl implements MemberMapper {

    @Override
    public Member toEntity(MemberRequest request) {
        if ( request == null ) {
            return null;
        }

        Member.MemberBuilder member = Member.builder();

        member.firstName( request.getFirstName() );
        member.lastName( request.getLastName() );
        member.email( request.getEmail() );
        member.phone( request.getPhone() );
        member.dateOfBirth( request.getDateOfBirth() );
        member.gender( request.getGender() );
        member.maritalStatus( request.getMaritalStatus() );
        member.addressLine1( request.getAddressLine1() );
        member.addressLine2( request.getAddressLine2() );
        member.city( request.getCity() );
        member.state( request.getState() );
        member.zipCode( request.getZipCode() );
        member.country( request.getCountry() );
        member.occupation( request.getOccupation() );
        member.notes( request.getNotes() );
        member.joinDate( request.getJoinDate() );
        member.baptismDate( request.getBaptismDate() );

        member.membershipStatus( request.getMembershipStatus() != null ? request.getMembershipStatus() : com.churchflow360.memberservice.model.MemberStatus.VISITOR );

        return member.build();
    }

    @Override
    public void updateEntityFromRequest(UpdateMemberRequest request, Member member) {
        if ( request == null ) {
            return;
        }

        if ( request.getFirstName() != null ) {
            member.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            member.setLastName( request.getLastName() );
        }
        if ( request.getEmail() != null ) {
            member.setEmail( request.getEmail() );
        }
        if ( request.getPhone() != null ) {
            member.setPhone( request.getPhone() );
        }
        if ( request.getDateOfBirth() != null ) {
            member.setDateOfBirth( request.getDateOfBirth() );
        }
        if ( request.getGender() != null ) {
            member.setGender( request.getGender() );
        }
        if ( request.getMaritalStatus() != null ) {
            member.setMaritalStatus( request.getMaritalStatus() );
        }
        if ( request.getAddressLine1() != null ) {
            member.setAddressLine1( request.getAddressLine1() );
        }
        if ( request.getAddressLine2() != null ) {
            member.setAddressLine2( request.getAddressLine2() );
        }
        if ( request.getCity() != null ) {
            member.setCity( request.getCity() );
        }
        if ( request.getState() != null ) {
            member.setState( request.getState() );
        }
        if ( request.getZipCode() != null ) {
            member.setZipCode( request.getZipCode() );
        }
        if ( request.getCountry() != null ) {
            member.setCountry( request.getCountry() );
        }
        if ( request.getOccupation() != null ) {
            member.setOccupation( request.getOccupation() );
        }
        if ( request.getNotes() != null ) {
            member.setNotes( request.getNotes() );
        }
        if ( request.getJoinDate() != null ) {
            member.setJoinDate( request.getJoinDate() );
        }
        if ( request.getBaptismDate() != null ) {
            member.setBaptismDate( request.getBaptismDate() );
        }
        if ( request.getWaterBaptized() != null ) {
            member.setWaterBaptized( request.getWaterBaptized() );
        }
    }

    @Override
    public MemberResponse toResponse(Member member) {
        if ( member == null ) {
            return null;
        }

        MemberResponse memberResponse = new MemberResponse();

        memberResponse.setId( member.getId() );
        memberResponse.setFirstName( member.getFirstName() );
        memberResponse.setLastName( member.getLastName() );
        memberResponse.setEmail( member.getEmail() );
        memberResponse.setPhone( member.getPhone() );
        memberResponse.setDateOfBirth( member.getDateOfBirth() );
        memberResponse.setGender( member.getGender() );
        memberResponse.setMaritalStatus( member.getMaritalStatus() );
        memberResponse.setAddressLine1( member.getAddressLine1() );
        memberResponse.setAddressLine2( member.getAddressLine2() );
        memberResponse.setCity( member.getCity() );
        memberResponse.setState( member.getState() );
        memberResponse.setZipCode( member.getZipCode() );
        memberResponse.setCountry( member.getCountry() );
        memberResponse.setProfilePhoto( member.getProfilePhoto() );
        memberResponse.setOccupation( member.getOccupation() );
        memberResponse.setNotes( member.getNotes() );
        memberResponse.setMembershipStatus( member.getMembershipStatus() );
        memberResponse.setJoinDate( member.getJoinDate() );
        memberResponse.setBaptismDate( member.getBaptismDate() );
        memberResponse.setWaterBaptized( member.getWaterBaptized() );
        memberResponse.setCreatedBy( member.getCreatedBy() );
        memberResponse.setCreatedAt( member.getCreatedAt() );
        memberResponse.setUpdatedAt( member.getUpdatedAt() );

        memberResponse.setFullName( member.getFullName() );
        memberResponse.setMembershipNumber( member.getMembershipNumber() );

        return memberResponse;
    }
}
