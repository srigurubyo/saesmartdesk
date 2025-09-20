package com.sae.smartdesk.request.service;

import com.sae.smartdesk.auth.entity.User;
import com.sae.smartdesk.auth.service.UserService;
import com.sae.smartdesk.common.enums.RequestStatus;
import com.sae.smartdesk.common.enums.RequestType;
import com.sae.smartdesk.common.exception.NotFoundException;
import com.sae.smartdesk.request.dto.RequestDetailDto;
import com.sae.smartdesk.request.dto.RequestListItem;
import com.sae.smartdesk.request.dto.RequestSearchCriteria;
import com.sae.smartdesk.request.entity.Request;
import com.sae.smartdesk.request.mapper.RequestMapper;
import com.sae.smartdesk.request.repository.RequestRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RequestQueryService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final RequestAuthorizationService authorizationService;
    private final UserService userService;

    public RequestQueryService(RequestRepository requestRepository, RequestMapper requestMapper,
                               RequestAuthorizationService authorizationService, UserService userService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    public RequestDetailDto getRequest(UUID requestId, UUID actorId) {
        Request request = requestRepository.findDetailedById(requestId)
            .orElseThrow(() -> new NotFoundException("Request not found"));
        User actor = userService.getById(actorId);
        authorizationService.ensureCanView(request, actor);
        return requestMapper.toDetail(request);
    }

    public Page<RequestListItem> search(RequestSearchCriteria criteria, UUID actorId, Pageable pageable) {
        User actor = userService.getById(actorId);
        Specification<Request> specification = buildSpecification(criteria, actor);
        return requestRepository.findAll(specification, pageable)
            .map(requestMapper::toListItem);
    }

    private Specification<Request> buildSpecification(RequestSearchCriteria criteria, User actor) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.type() != null) {
                predicates.add(cb.equal(root.get("requestType"), criteria.type()));
            }
            if (criteria.status() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.status()));
            }
            if (criteria.from() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), criteria.from()));
            }
            if (criteria.to() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("submittedAt"), criteria.to()));
            }

            boolean isAdmin = actor.getRoles().stream()
                .map(role -> role.getName().toUpperCase(Locale.ROOT))
                .anyMatch(name -> name.equals("ADMIN"));
            boolean mineOnly = Boolean.TRUE.equals(criteria.mine());
            if (mineOnly || !isAdmin) {
                predicates.add(cb.equal(root.get("requester").get("id"), actor.getId()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
