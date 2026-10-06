package com.churchflow360.memberservice.mapper;

import com.churchflow360.memberservice.dto.request.MemberRequest;
import com.churchflow360.memberservice.dto.request.UpdateMemberRequest;
import com.churchflow360.memberservice.dto.response.MemberResponse;
import com.churchflow360.memberservice.model.Member;
import com.churchflow360.memberservice.model.MemberStatus;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MemberMapper {

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "membershipStatus",
            expression = "java(request.getMembershipStatus() != null " +
                    "? request.getMembershipStatus() " +
                    ": com.churchflow360.memberservice.model.MemberStatus.VISITOR)")
    Member toEntity(MemberRequest request);

    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "membershipNumber", ignore = true)
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    @Mapping(target = "createdBy",        ignore = true)
    @Mapping(target = "membershipStatus", ignore = true)
    void updateEntityFromRequest(UpdateMemberRequest request, @MappingTarget Member member);

    @Mapping(target = "fullName",
            expression = "java(member.getFullName())")
    @Mapping(target = "membershipNumber",
            expression = "java(member.getMembershipNumber())")
    MemberResponse toResponse(Member member);
}