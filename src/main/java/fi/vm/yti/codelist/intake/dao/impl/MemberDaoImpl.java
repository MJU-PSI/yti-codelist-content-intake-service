package fi.vm.yti.codelist.intake.dao.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.codelist.common.dto.CodeDTO;
import fi.vm.yti.codelist.common.dto.ErrorModel;
import fi.vm.yti.codelist.common.dto.ExtensionDTO;
import fi.vm.yti.codelist.common.dto.MemberDTO;
import fi.vm.yti.codelist.common.dto.MemberValueDTO;
import fi.vm.yti.codelist.intake.api.ApiUtils;
import fi.vm.yti.codelist.intake.configuration.UriProperties;
import fi.vm.yti.codelist.intake.dao.CodeDao;
import fi.vm.yti.codelist.intake.dao.CodeSchemeDao;
import fi.vm.yti.codelist.intake.dao.ExtensionDao;
import fi.vm.yti.codelist.intake.dao.MemberDao;
import fi.vm.yti.codelist.intake.dao.MemberValueDao;
import fi.vm.yti.codelist.intake.dao.ValueTypeDao;
import fi.vm.yti.codelist.intake.exception.NotFoundException;
import fi.vm.yti.codelist.intake.exception.YtiCodeListException;
import fi.vm.yti.codelist.intake.jpa.MemberRepository;
import fi.vm.yti.codelist.intake.language.LanguageService;
import fi.vm.yti.codelist.intake.log.EntityChangeLogger;
import fi.vm.yti.codelist.intake.model.Code;
import fi.vm.yti.codelist.intake.model.CodeScheme;
import fi.vm.yti.codelist.intake.model.Extension;
import fi.vm.yti.codelist.intake.model.Member;
import fi.vm.yti.codelist.intake.model.MemberValue;
import fi.vm.yti.codelist.intake.model.ValueType;
import static fi.vm.yti.codelist.common.constants.ApiConstants.CODE_EXTENSION;
import static fi.vm.yti.codelist.intake.exception.ErrorConstants.*;
import static fi.vm.yti.codelist.intake.util.EncodingUtils.urlEncodeCodeValue;

@Component
public class MemberDaoImpl extends AbstractDao implements MemberDao {
    private static final Logger LOG = LoggerFactory.getLogger(MemberDaoImpl.class);

    private static final String LOCALNAME_CROSS_REFERENCE_LIST = "crossReferenceList";
    private static final String PREFIX_FOR_EXTENSION_SEQUENCE_NAME = "seq_for_ext_";
    private static final int MAX_LEVEL = 15;
    private static final int MAX_LEVEL_FOR_CROSS_REFERENCE_LIST = 2;
    private static final String CODE_PREFIX = "code:";
    private static final String MEMBER_PREFIX = "member:";

    private final EntityChangeLogger entityChangeLogger;
    private final MemberRepository memberRepository;
    private final CodeDao codeDao;
    private final CodeSchemeDao codeSchemeDao;
    private final UriProperties uriProperties;
    private final LanguageService languageService;
    private final MemberValueDao memberValueDao;
    private final ApiUtils apiUtils;
    private final ExtensionDao extensionDao;
    private final ValueTypeDao valueTypeDao;

    @Inject
    public MemberDaoImpl(final EntityChangeLogger entityChangeLogger,
                         final MemberRepository memberRepository,
                         final CodeDao codeDao,
                         final CodeSchemeDao codeSchemeDao,
                         final UriProperties uriProperties,
                         final LanguageService languageService,
                         final MemberValueDao memberValueDao,
                         final ApiUtils apiUtils,
                         @Lazy final ExtensionDao extensionDao,
                         final ValueTypeDao valueTypeDao) {
        super(languageService);
        this.entityChangeLogger = entityChangeLogger;
        this.memberRepository = memberRepository;
        this.codeDao = codeDao;
        this.codeSchemeDao = codeSchemeDao;
        this.uriProperties = uriProperties;
        this.languageService = languageService;
        this.memberValueDao = memberValueDao;
        this.apiUtils = apiUtils;
        this.extensionDao = extensionDao;
        this.valueTypeDao = valueTypeDao;
    }

    @Transactional
    public void delete(final Member member) {
        entityChangeLogger.logMemberChange(member);
        codeSchemeDao.updateContentModified(member.getExtension().getParentCodeScheme().getId());
        memberRepository.delete(member);
    }

    @Transactional
    public void delete(final Set<Member> members) {
        entityChangeLogger.logMemberChanges(members);
        if (!members.isEmpty()) {
            final UUID codeSchemeId = members.iterator().next().getExtension().getParentCodeScheme().getId();
            codeSchemeDao.updateContentModified(codeSchemeId);
        }
        memberRepository.deleteAll(members);
    }

    @Transactional
    public void save(final Member member) {
        memberRepository.save(member);
        entityChangeLogger.logMemberChange(member);
    }

    @Transactional
    public void save(final Set<Member> members,
                     final boolean logChange) {
        memberRepository.saveAll(members);
        if (logChange) {
            entityChangeLogger.logMemberChanges(members);
        }
    }

    @Transactional
    public void save(final Set<Member> members) {
        save(members, true);
    }

    @Transactional
    public Set<Member> findAll() {
        return memberRepository.findAll();
    }

    @Transactional
    public Set<Member> findAll(final PageRequest pageRequest) {
        return new HashSet<>(memberRepository.findAll(pageRequest).getContent());
    }

    @Transactional
    public Member findById(final UUID id) {
        return memberRepository.findById(id);
    }

    @Transactional
    public Set<Member> findByCodeId(final UUID id) {
        return memberRepository.findByCodeId(id);
    }

    @Transactional
    public Set<Member> findByRelatedMemberId(final UUID id) {
        return memberRepository.findByRelatedMemberId(id);
    }

    @Transactional
    public Set<Member> findByRelatedMemberCode(final Code code) {
        return memberRepository.findByRelatedMemberCode(code);
    }

    @Transactional
    public Set<Member> findByExtensionId(final UUID extensionId) {
        return memberRepository.findByExtensionId(extensionId);
    }

    @Transactional
    public Set<Member> findByExtensionIdAndCodeId(final UUID extensionId,
                                                  final UUID codeId) {
        return memberRepository.findByExtensionIdAndCodeId(extensionId, codeId);
    }

    @Transactional
    public Set<Member> updateMemberEntityFromDto(final Extension extension,
                                                 final MemberDTO memberDto) {
        final Set<MemberDTO> memberDtos = new HashSet<>();
        memberDtos.add(memberDto);
        return updateMemberEntitiesFromDtos(extension, memberDtos);
    }

    private Map<String, ValueType> getValueTypeMap() {
        final Map<String, ValueType> valueTypeMap = new HashMap<>();
        valueTypeDao.findAll().forEach(valueType -> valueTypeMap.put(valueType.getLocalName(), valueType));
        return valueTypeMap;
    }

    @Transactional
    public Set<Member> updateMemberEntitiesFromDtos(final Extension extension,
                                                    final Set<MemberDTO> memberDtos) {
        final Map<String, ValueType> valueTypeMap = getValueTypeMap();
        final Set<Member> affectedMembers = new HashSet<>();
        final Map<String, Code> parentCodeSchemeCodesMap = new HashMap<>();
        final CodeScheme parentCodeScheme = extension.getParentCodeScheme();
        final Set<Code> parentCodeSchemeCodes = codeDao.findByCodeSchemeId(parentCodeScheme.getId());
        parentCodeSchemeCodes.forEach(code -> parentCodeSchemeCodesMap.put(code.getUri(), code));
        final Set<CodeScheme> allowedCodeSchemes = gatherAllowedCodeSchemes(parentCodeScheme, extension);
        final Set<Member> membersToBeStored = new HashSet<>();
        final Integer origNextSequenceId = getNextValueForMemberSequence(extension);
        final MutableInt nextSequenceId = new MutableInt(origNextSequenceId);
        if (memberDtos != null) {
            final Set<Member> existingMembers = findByExtensionId(extension.getId());
            for (final MemberDTO memberDto : memberDtos) {
                final Member member = createOrUpdateMember(extension, existingMembers, parentCodeSchemeCodesMap, allowedCodeSchemes, memberDto, affectedMembers, nextSequenceId);
                existingMembers.add(member);
                memberDto.setId(member.getId());
                affectedMembers.add(member);
                updateMemberMemberValues(extension, member, memberDto, valueTypeMap);
                membersToBeStored.add(member);
                resolveAffectedRelatedMembers(existingMembers, affectedMembers, member.getId());
            }
            save(membersToBeStored);
            resolveMemberRelations(extension, existingMembers, affectedMembers, memberDtos);
        }
        if (!affectedMembers.isEmpty()) {
            codeSchemeDao.updateContentModified(extension.getParentCodeScheme().getId());
        }
        if (nextSequenceId.toInteger() >= origNextSequenceId) {
            setMemberSequence(extension, nextSequenceId.toInteger() - 1);
        }
        return affectedMembers;
    }

    private void resolveAffectedRelatedMembers(final Set<Member> existingMembers,
                                               final Set<Member> affectedMembers,
                                               final UUID memberId) {
        final Set<Member> relatedMembers = new HashSet<>();
        existingMembers.forEach(member -> {
            if (member.getId().equals(memberId)) {
                relatedMembers.add(member);
            }
        });
        affectedMembers.addAll(relatedMembers);
    }

    private void updateMemberMemberValues(final Extension extension,
                                          final Member member,
                                          final MemberDTO fromMemberDto,
                                          final Map<String, ValueType> valueTypeMap) {
        final Set<MemberValue> memberValues;
        try {
            final Set<MemberValueDTO> memberValueDtos = fromMemberDto.getMemberValues();
            if (memberValueDtos != null && !memberValueDtos.isEmpty()) {
                memberValues = memberValueDao.updateMemberValueEntitiesFromDtos(member, fromMemberDto.getMemberValues(), valueTypeMap);
            } else {
                memberValues = new HashSet<>();
            }
        } catch (final Exception e) {
            if (e.getMessage().contains("unique_order")) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_CODE_ORDER_CONTAINS_DUPLICATE_VALUES));
            } else {
                throw e;
            }
        }
        ensureThatRequiredMemberValuesArePresent(extension.getPropertyType().getValueTypes(), memberValues);
        if (member.getMemberValues() != null) {
            member.getMemberValues().clear();
            member.getMemberValues().addAll(memberValues);
        } else {
            member.setMemberValues(memberValues);
        }
    }

    private void ensureThatRequiredMemberValuesArePresent(final Set<ValueType> valueTypes,
                                                          final Set<MemberValue> memberValues) {
        if (!valueTypes.isEmpty()) {
            final Set<ValueType> requiredValueTypes = valueTypes.stream().filter(ValueType::getRequired).collect(Collectors.toSet());
            if (!requiredValueTypes.isEmpty()) {
                requiredValueTypes.forEach(valueType -> {
                    if (!memberValues.isEmpty()) {
                        if (memberValues.stream().noneMatch(member -> member.getValueType() == valueType)) {
                            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBERVALUE_NOT_SET));
                        }
                    } else {
                        throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBERVALUE_NOT_SET));
                    }
                });
            }
        }
    }

    private UUID getUuidFromString(final String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (final IllegalArgumentException e) {
            LOG.debug("Exception constructing UUID: " + e.getMessage());
            return null;
        }
    }

    private void checkDuplicateCode(final Set<Member> members,
                                    final String identifier) {
        boolean found = false;
        for (final Member member : members) {
            final Code code = member.getCode();
            if (code == null) {
                throw new NotFoundException();
            }
            if ((identifier.startsWith(uriProperties.getUriHostPathAddress()) && code.getUri().equalsIgnoreCase(identifier)) ||
                (code.getCodeValue().equalsIgnoreCase(identifier) && code.getCodeScheme().getId().equals(member.getExtension().getParentCodeScheme().getId()))) {
                if (found) {
                    throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBERS_HAVE_DUPLICATE_CODE_USE_MEMBER_ID));
                }
                found = true;
            }
        }
    }

    private void linkMemberWithId(final Extension extension,
                                  final Member member,
                                  final UUID id) {
        final Member relatedMember = findById(id);
        if (relatedMember != null) {
            if (extension.getId().equals(relatedMember.getExtension().getId())) {
                linkMembers(member, relatedMember, id.toString());
            } else {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_LINKED_FROM_ANOTHER_EXTENSION));
            }
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_NOT_FOUND_WITH_UUID, id.toString()));
        }
    }

    private void linkMembers(final Member member,
                             final Member relatedMember,
                             final String identifier) {
        if (relatedMember != null) {
            member.setRelatedMember(relatedMember);
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_FOUND_WITH_IDENTIFIER, identifier));
        }
    }

    private Set<Member> resolveMemberRelation(final Extension extension,
                                              final Set<Member> existingMembers,
                                              final Member member,
                                              final MemberDTO fromMember) {
        final MemberDTO relatedMember = fromMember.getRelatedMember();
        if (CODE_EXTENSION.equalsIgnoreCase(extension.getPropertyType().getContext()) && relatedMember != null) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_RELATION_NOT_ALLOWED_FOR_CODE_EXTENSION));
        }
        if (relatedMember != null && relatedMember.getId() != null && fromMember.getId() != null && member.getId().equals(relatedMember.getId())) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_RELATION_SET_TO_ITSELF));
        }
        final Set<Member> linkedMembers = new HashSet<>();
        if (relatedMember != null && relatedMember.getId() != null) {
            linkMemberWithId(extension, member, relatedMember.getId());
            linkedMembers.add(member);
        } else if (relatedMember != null && relatedMember.getCode() != null) {
            final String memberRelationUriIdentifier = relatedMember.getCode().getUri();
            final String memberCodeCodeValueIdentifier = relatedMember.getCode().getCodeValue();
            UUID uuid = null;
            if (memberCodeCodeValueIdentifier != null) {
                uuid = getUuidFromString(memberCodeCodeValueIdentifier);
            }
            if (uuid != null) {
                linkMemberWithId(extension, member, uuid);
                linkedMembers.add(member);
            } else if (memberRelationUriIdentifier != null && memberRelationUriIdentifier.startsWith(extension.getUri())) {
                boolean found = false;
                for (final Member existingMember : existingMembers) {
                    if (existingMember.getUri().equalsIgnoreCase(memberRelationUriIdentifier)) {
                        linkMembers(member, existingMember, memberRelationUriIdentifier);
                        linkedMembers.add(member);
                        found = true;
                    }
                }
                if (!found) {
                    throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_NOT_FOUND_WITH_URI, memberRelationUriIdentifier));
                }
            } else if (memberRelationUriIdentifier != null && memberRelationUriIdentifier.startsWith(uriProperties.getUriHostPathAddress())) {
                boolean found = false;
                for (final Member existingMember : existingMembers) {
                    final Code existingMemberCode = existingMember.getCode();
                    if (memberRelationUriIdentifier.startsWith(uriProperties.getUriHostPathAddress()) && existingMemberCode != null && existingMemberCode.getUri().equalsIgnoreCase(memberRelationUriIdentifier)) {
                        checkDuplicateCode(existingMembers, memberRelationUriIdentifier);
                        linkMembers(member, existingMember, memberRelationUriIdentifier);
                        linkedMembers.add(member);
                        found = true;
                    }
                }
                if (!found) {
                    throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_FOUND_WITH_IDENTIFIER, memberRelationUriIdentifier));
                }
            } else if (memberCodeCodeValueIdentifier != null && !memberCodeCodeValueIdentifier.isEmpty()) {
                if (memberCodeCodeValueIdentifier.startsWith(CODE_PREFIX) && memberCodeCodeValueIdentifier.length() > CODE_PREFIX.length()) {
                    final String codeValue = memberCodeCodeValueIdentifier.substring(CODE_PREFIX.length());
                    boolean found = false;
                    for (final Member extensionMember : existingMembers) {
                        final Code existingMemberCode = extensionMember.getCode();
                        if (existingMemberCode != null && existingMemberCode.getCodeValue().equalsIgnoreCase(codeValue) && existingMemberCode.getCodeScheme().getId().equals(extension.getParentCodeScheme().getId())) {
                            checkDuplicateCode(existingMembers, codeValue);
                            linkMembers(member, extensionMember, codeValue);
                            linkedMembers.add(member);
                            found = true;
                        }
                    }
                    if (!found) {
                        throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_FOUND_WITH_IDENTIFIER, memberCodeCodeValueIdentifier));
                    }
                } else {
                    final String memberSequenceId;
                    if (memberCodeCodeValueIdentifier.startsWith(MEMBER_PREFIX) && memberCodeCodeValueIdentifier.length() > MEMBER_PREFIX.length()) {
                        memberSequenceId = memberCodeCodeValueIdentifier.substring(MEMBER_PREFIX.length());
                    } else {
                        memberSequenceId = memberCodeCodeValueIdentifier;
                    }
                    if (isStringInt(memberSequenceId)) {
                        boolean found = false;
                        for (final Member existingMember : existingMembers) {
                            if (existingMember.getSequenceId().equals(Integer.parseInt(memberSequenceId))) {
                                linkMembers(member, existingMember, memberRelationUriIdentifier);
                                linkedMembers.add(member);
                                found = true;
                            }
                        }
                        if (!found) {
                            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_NOT_FOUND_WITH_MEMBER_ID, memberCodeCodeValueIdentifier));
                        }
                    } else {
                        throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_RELATION_ERROR, memberCodeCodeValueIdentifier));
                    }
                }
            }
        } else if (relatedMember == null) {
            member.setRelatedMember(null);
        }
        linkedMembers.forEach(mem -> validateMemberHierarchyLevels(mem, extension));
        return linkedMembers;
    }

    private boolean isStringInt(final String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (final NumberFormatException e) {
            LOG.debug("Exception parsing int: " + e.getMessage());
            return false;
        }
    }

    private void validateMemberHierarchyLevels(final Member member,
                                               final Extension extension) {
        final Set<Member> chainedMembers = new HashSet<>();
        chainedMembers.add(member);
        validateMemberHierarchyLevels(chainedMembers, member, 1, extension);
    }

    private void validateMemberHierarchyLevels(final Set<Member> chainedMembers,
                                               final Member member,
                                               final int level,
                                               final Extension extension) {
        if (LOCALNAME_CROSS_REFERENCE_LIST.equalsIgnoreCase(extension.getPropertyType().getLocalName())) {
            if (level > MAX_LEVEL_FOR_CROSS_REFERENCE_LIST) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_HIERARCHY_MAXLEVEL_REACHED));
            }
        } else {
            if (level > MAX_LEVEL) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_HIERARCHY_MAXLEVEL_REACHED));
            }
        }

        final Member relatedMember = member.getRelatedMember();
        if (relatedMember != null) {
            if (chainedMembers.contains(relatedMember)) {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CYCLIC_DEPENDENCY_ISSUE, relatedMember.getUri()));
            }
            chainedMembers.add(relatedMember);
            validateMemberHierarchyLevels(chainedMembers, relatedMember, level + 1, extension);
        }
    }

    private void resolveMemberRelations(final Extension extension,
                                        final Set<Member> existingMembers,
                                        final Set<Member> members,
                                        final Set<MemberDTO> fromMembers) {
        final Set<Member> linkedMembersToBeStored = new HashSet<>();
        fromMembers.forEach(fromMember -> {
            Member member = null;
            for (final Member mem : members) {
                if (fromMember.getId().equals(mem.getId())) {
                    member = mem;
                }
            }
            if (member != null) {
                final Set<Member> linkedMembers = resolveMemberRelation(extension, existingMembers, member, fromMember);
                if (!linkedMembers.isEmpty()) {
                    linkedMembersToBeStored.addAll(linkedMembers);
                }
            }
        });
        save(linkedMembersToBeStored, false);
    }

    @Transactional
    public Member createOrUpdateMember(final Extension extension,
                                       final Set<Member> existingMembers,
                                       final Map<String, Code> codesMap,
                                       final Set<CodeScheme> allowedCodeSchemes,
                                       final MemberDTO fromMember,
                                       final Set<Member> members,
                                       final MutableInt nextSequenceId) {
        Member existingMember = null;
        if (extension != null) {
            if (fromMember.getId() != null || fromMember.getSequenceId() != null) {
                for (final Member member : existingMembers) {
                    if (member.getId().equals(fromMember.getId()) || member.getSequenceId().equals(fromMember.getSequenceId())) {
                        existingMember = member;
                        break;
                    }
                }
                if (existingMember != null) {
                    validateExtensionMatch(existingMember, extension);
                }
            }
            validateMultipleLinkedCodesForCodeExtensionMembers(extension, existingMembers, existingMember, fromMember);
            final Member member;
            if (existingMember != null) {
                member = updateMember(extension.getParentCodeScheme(), existingMembers, codesMap, allowedCodeSchemes, existingMember, fromMember, members);
            } else {
                member = createMember(extension.getParentCodeScheme(), existingMembers, codesMap, allowedCodeSchemes, extension, fromMember, members, nextSequenceId);
            }
            return member;
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_EXTENSION_NOT_FOUND));
        }
    }

    private void validateMultipleLinkedCodesForCodeExtensionMembers(final Extension extension,
                                                                    final Set<Member> existingMembers,
                                                                    final Member existingMember,
                                                                    final MemberDTO fromMember) {
        if (CODE_EXTENSION.equalsIgnoreCase(extension.getPropertyType().getContext())) {
            for (final Member member : existingMembers) {
                if (existingMember != null && (existingMember.getSequenceId().equals(member.getSequenceId()) || existingMember.getId().equals(member.getId()))) {
                    continue;
                }
                final Code code = member.getCode();
                final String uriIdentifier = fromMember.getCode().getUri();
                final String codeValueIdentifier = fromMember.getCode().getCodeValue();
                if ((fromMember.getCode().getId() != null && fromMember.getCode().getId().equals(code.getId())) ||
                    (uriIdentifier != null && uriIdentifier.startsWith(uriProperties.getUriHostPathAddress()) && code.getUri().equalsIgnoreCase(uriIdentifier)) ||
                    (code.getCodeScheme().getId().equals(extension.getParentCodeScheme().getId()) && code.getCodeValue().equalsIgnoreCase(codeValueIdentifier))) {
                    throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_CODE_EXTENSION_MULTIPLE_MEMBERS));
                }
            }
        }
    }

    private Member updateMember(final CodeScheme codeScheme,
                                final Set<Member> existingMembers,
                                final Map<String, Code> codesMap,
                                final Set<CodeScheme> allowedCodeSchemes,
                                final Member existingMember,
                                final MemberDTO fromMember,
                                final Set<Member> affectedMembers) {
        mapPrefLabel(fromMember, existingMember, codeScheme);
        if (fromMember.getOrder() != null && !Objects.equals(existingMember.getOrder(), fromMember.getOrder())) {
            checkOrderAndShiftExistingMemberOrderIfInUse(existingMembers, fromMember.getOrder(), affectedMembers);
            existingMember.setOrder(fromMember.getOrder());
        } else if (existingMember.getOrder() == null && fromMember.getOrder() == null) {
            existingMember.setOrder(findNextOrderFromMembers(existingMembers));
        }
        if (fromMember.getCode() != null) {
            final Code code = findCodeUsingCodeValueOrUri(codeScheme, codesMap, allowedCodeSchemes, fromMember);
            if (!Objects.equals(existingMember.getCode(), code)) {
                existingMember.setCode(code);
            }
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_SET));
        }
        if (!Objects.equals(existingMember.getStartDate(), fromMember.getStartDate())) {
            existingMember.setStartDate(fromMember.getStartDate());
        }
        if (!Objects.equals(existingMember.getEndDate(), fromMember.getEndDate())) {
            existingMember.setEndDate(fromMember.getEndDate());
        }
        existingMember.setModified(new Date(System.currentTimeMillis()));
        return existingMember;
    }

    private Member createMember(final CodeScheme codeScheme,
                                final Set<Member> existingMembers,
                                final Map<String, Code> codesMap,
                                final Set<CodeScheme> allowedCodeSchemes,
                                final Extension extension,
                                final MemberDTO fromMember,
                                final Set<Member> affectedMembers,
                                final MutableInt nextSequenceId) {
        final Member member = new Member();
        if (fromMember.getId() != null) {
            member.setId(fromMember.getId());
        } else {
            final UUID uuid = UUID.randomUUID();
            member.setId(uuid);
        }
        mapPrefLabel(fromMember, member, codeScheme);
        if (fromMember.getOrder() != null) {
            checkOrderAndShiftExistingMemberOrderIfInUse(existingMembers, fromMember.getOrder(), affectedMembers);
            member.setOrder(fromMember.getOrder());
        } else {
            member.setOrder(findNextOrderFromMembers(existingMembers));
        }
        if (fromMember.getCode() != null) {
            member.setCode(findCodeUsingCodeValueOrUri(codeScheme, codesMap, allowedCodeSchemes, fromMember));
        }
        member.setStartDate(fromMember.getStartDate());
        member.setEndDate(fromMember.getEndDate());
        member.setExtension(extension);
        final Date timeStamp = new Date(System.currentTimeMillis());
        member.setCreated(timeStamp);
        member.setModified(timeStamp);
        member.setSequenceId(resolveSequenceValue(fromMember, nextSequenceId));
        member.setUri(apiUtils.createMemberUri(member));
        return member;
    }

    private Integer resolveSequenceValue(final MemberDTO fromMember,
                                         final MutableInt nextSequenceId) {
        final Integer fromMemberSequenceId = fromMember.getSequenceId();
        if (fromMemberSequenceId != null) {
            if (fromMemberSequenceId >= nextSequenceId.toInteger()) {
                nextSequenceId.setValue(fromMemberSequenceId + 1);
            }
            return fromMemberSequenceId;
        } else {
            final Integer nextSeqId = nextSequenceId.toInteger();
            nextSequenceId.setValue(nextSeqId + 1);
            return nextSeqId;
        }
    }

    private String constructSequenceName(final Extension extension) {
        final String postFixPartOfTheSequenceName = extension.getId().toString().replaceAll("-", "_");
        return PREFIX_FOR_EXTENSION_SEQUENCE_NAME + postFixPartOfTheSequenceName;
    }

    private Integer getNextValueForMemberSequence(final Extension extension) {
        return memberRepository.getNextMemberSequenceId(constructSequenceName(extension));
    }

    private Integer setMemberSequence(final Extension extension,
                                      final Integer value) {
        return memberRepository.setMemberSequenceId(constructSequenceName(extension), value);
    }

    private void validateExtensionMatch(final Member member,
                                        final Extension extension) {
        if (member.getExtension() != extension) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_EXTENSION_DOES_NOT_MATCH));
        }
    }

    private Code findCodeUsingCodeValueOrUri(final CodeScheme parentCodeScheme,
                                             final Map<String, Code> codesMap,
                                             final Set<CodeScheme> allowedCodeSchemes,
                                             final MemberDTO member) {
        final CodeDTO fromCode = member.getCode();
        final Code code;
        final String codeUri = resolveCodeUri(parentCodeScheme, fromCode);
        if (codeUri != null && !codeUri.isEmpty()) {
            if (codesMap.containsKey(codeUri)) {
                code = codesMap.get(codeUri);
            } else {
                code = codeDao.findByUri(codeUri);
            }
            if (code != null) {
                checkThatCodeIsInAllowedCodeScheme(code.getCodeScheme(), allowedCodeSchemes);
            } else {
                throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_FOUND_WITH_IDENTIFIER, codeUri));
            }
        } else {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_FOUND));
        }
        return code;
    }

    private String resolveCodeUri(final CodeScheme parentCodeScheme,
                                  final CodeDTO fromCode) {
        final String codeUri;
        if (fromCode.getUri() != null) {
            codeUri = fromCode.getUri();
        } else if (fromCode.getCodeValue() != null && !fromCode.getCodeValue().isEmpty()) {
            codeUri = createCodeUriForCodeScheme(parentCodeScheme, urlEncodeCodeValue(fromCode.getCodeValue()));
        } else {
            codeUri = null;
        }
        return codeUri;
    }

    private String createCodeUriForCodeScheme(final CodeScheme codeScheme,
                                              final String codeValue) {
        return codeScheme.getUri() + "/code/" + codeValue;
    }

    private void checkThatCodeIsInAllowedCodeScheme(final CodeScheme codeSchemeForCode,
                                                    final Set<CodeScheme> allowedCodeSchemes) {
        if (!allowedCodeSchemes.contains(codeSchemeForCode)) {
            throw new YtiCodeListException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), ERR_MSG_USER_MEMBER_CODE_NOT_ALLOWED));
        }
    }

    private void checkOrderAndShiftExistingMemberOrderIfInUse(final Set<Member> existingMembers,
                                                              final Integer order,
                                                              final Set<Member> affectedMembers) {
        final Set<Member> membersWithOrder = existingMembers.stream().filter(member -> member.getOrder().equals(order)).collect(Collectors.toSet());
        membersWithOrder.forEach(member -> {
            if (member.getOrder() != null && member.getOrder().equals(order)) {
                member.setOrder(findNextOrderFromMembers(existingMembers));
                affectedMembers.add(member);
            }
        });
    }

    private Integer findNextOrderFromMembers(final Set<Member> existingMembers) {
        Integer maxOrder = 0;
        for (final Member member : existingMembers) {
            final Integer memberOrder = member.getOrder();
            if (memberOrder != null && member.getOrder() > maxOrder) {
                maxOrder = memberOrder;
            }
        }
        return maxOrder + 1;
    }

    public Integer getNextOrderInSequence(final Extension extension) {
        final Integer maxOrder = memberRepository.getMemberMaxOrder(extension.getId());
        if (maxOrder == null) {
            return 1;
        } else {
            return maxOrder + 1;
        }
    }

    private Set<CodeScheme> gatherAllowedCodeSchemes(final CodeScheme parentCodeScheme,
                                                     final Extension extension) {
        final Set<CodeScheme> allowedCodeSchemes = new HashSet<>();
        allowedCodeSchemes.add(parentCodeScheme);
        final Set<CodeScheme> extensionCodeSchemes = extension.getCodeSchemes();
        if (extensionCodeSchemes != null && !extensionCodeSchemes.isEmpty()) {
            allowedCodeSchemes.addAll(extension.getCodeSchemes());
        }
        return allowedCodeSchemes;
    }

    @Transactional
    public Set<Member> createMissingMembersForAllCodesOfAllCodelistsOfAnExtension(final ExtensionDTO extensionDTO) {
        Extension extension = extensionDao.findById(extensionDTO.getId());
        LinkedHashSet<Member> createdMembers = new LinkedHashSet<>();
        LinkedHashSet<CodeScheme> codeSchemes = new LinkedHashSet<>();
        codeSchemes.add(extension.getParentCodeScheme());
        codeSchemes.addAll(extension.getCodeSchemes());

        LinkedHashSet<Code> codesWithMembersInThisExtension = new LinkedHashSet<>();
        LinkedHashSet<Code> codesWithNoMembersInThisExtension = new LinkedHashSet<>();

        final LinkedHashMap<CodeScheme, LinkedHashSet<Code>> codeSchemesWithCodesOrdered = new LinkedHashMap<>();
        codeSchemes.forEach(cs -> populateMapWhereCodesAreOrderedBasedOnFlatOrderAscending(cs, codeSchemesWithCodesOrdered));

        codeSchemesWithCodesOrdered.keySet().forEach(cs ->
            cs.getCodes().forEach(c ->
                c.getMembers().forEach(member -> {
                    if (member.getExtension().getId().compareTo(extension.getId()) == 0) {
                        codesWithMembersInThisExtension.add(c);
                    }
                })
            )
        );

        codeSchemesWithCodesOrdered.keySet().forEach(cs ->
            cs.getCodes().forEach(c -> {
                if (!codesWithMembersInThisExtension.contains(c)) {
                    codesWithNoMembersInThisExtension.add(c);
                }
            })
        );

        final Date timeStamp = new Date(System.currentTimeMillis());

        codeSchemesWithCodesOrdered.keySet().forEach(cs -> {
            LinkedHashSet<Code> codesInCorrectOrder = codeSchemesWithCodesOrdered.get(cs);
            codesInCorrectOrder.forEach(code -> {
                if (codesWithNoMembersInThisExtension.contains(code)) {
                    Member m = new Member();
                    m.setId(UUID.randomUUID());
                    m.setOrder(this.getNextOrderInSequence(extension));
                    m.setCode(code);
                    m.setRelatedMember(null);
                    m.setEndDate(code.getEndDate());
                    m.setStartDate(code.getStartDate());
                    m.setExtension(extension);
                    m.setMemberValues(null);
                    m.setPrefLabel(null);
                    m.setSequenceId(getNextValueForMemberSequence(extension));
                    m.setUri(apiUtils.createMemberUri(m));
                    m.setCreated(timeStamp);
                    m.setModified(timeStamp);
                    this.save(m);
                    createdMembers.add(m);
                }
            });
        });

        if (!createdMembers.isEmpty()) {
            codeSchemeDao.updateContentModified(extension.getParentCodeScheme().getId());
        }

        return createdMembers;
    }

    /**
     * The why and how of this ordering-trick is the following - when someone has previoously created an extension, and now the user chooses to create the missing members,
     * a member is created for every code of every codescheme involved (that is , the extensions parent codescheme, and all the attached codeschemes (0-n pieces), THAT DOES NOT YET
     * have a corresponding member.
     * <p>
     * For arguments sake lets assume there is the parent codescheme with codes a,b,c (and existing member b) and 1 other codescheme with codes d,e,f and also a member for code e.
     * <p>
     * In this case we end up with the order b,e,a,c,d,f. b and e are first because they already exist. Then, the parent codescheme is processed first, and from there we get
     * alphabetically (by codevalue) a, c. And then the other codeschemes, alphabetically according to the codevalue (although in this case there is only one codescheme) and from
     * there we get alphabetically d, f.
     */
    private void populateMapWhereCodesAreOrderedBasedOnFlatOrderAscending(final CodeScheme codeScheme,
                                                                          final HashMap<CodeScheme, LinkedHashSet<Code>> codeSchemesWithCodesOrdered) {
        List<Code> codesSorted = new ArrayList<>(codeScheme.getCodes());
        codesSorted.sort(Comparator.comparing(Code::getOrder));
        LinkedHashSet<Code> codesOrdered = new LinkedHashSet<>(codesSorted);
        codeSchemesWithCodesOrdered.put(codeScheme, codesOrdered);
    }

    @Transactional
    public int getMemberCount() {
        return memberRepository.getMemberCount();
    }

    private void mapPrefLabel(final MemberDTO fromMember,
                              final Member member,
                              final CodeScheme codeScheme) {
        final Map<String, String> prefLabel = validateAndAppendLanguagesForCodeScheme(fromMember.getPrefLabel(), codeScheme);
        member.setPrefLabel(prefLabel);
    }
}
