package org.egov.ewst.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.ewst.models.EwasteApplication;
import org.egov.ewst.models.EwasteRegistrationRequest;
import org.egov.ewst.models.user.User;
import org.egov.ewst.models.user.CreateUserRequest;
import org.egov.ewst.models.user.UserDetailResponse;
import org.egov.ewst.models.user.UserSearchRequest;
import org.egov.ewst.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.user.host}")
	private String userHost;

	@Value("${egov.user.context.path}")
	private String userContextPath;

	@Value("${egov.user.create.path}")
	private String userCreateEndpoint;

	@Value("${egov.user.search.path}")
	private String userSearchEndpoint;

	@Value("${egov.user.update.path}")
	private String userUpdateEndpoint;

	/**
	 * Creates user of the users of ewaste if it is not created already
	 * 
	 * @param request Ewaste Request received for creating application
	 */
	public void createUser(EwasteRegistrationRequest request) {

		EwasteApplication ewasteApplication = request.getEwasteApplication().get(0);
		RequestInfo requestInfo = request.getRequestInfo();
		Role role = getCitizenRole();
		User user = new User();
		addUserDefaultFields(ewasteApplication.getTenantId(), role, user);
		UserDetailResponse userDetailResponse = userExists(user, requestInfo);
		List<User> existingUsersFromService = userDetailResponse.getUser();
		Map<String, User> userMapFromSearch = existingUsersFromService.stream()
				.collect(Collectors.toMap(User::getUuid, Function.identity()));

		if (CollectionUtils.isEmpty(existingUsersFromService)) {

			user.setUserName(UUID.randomUUID().toString());
			userDetailResponse = createUser(requestInfo, user);

		} else {

			String uuid = user.getUuid();
			if (uuid != null && userMapFromSearch.containsKey(uuid)) {
				userDetailResponse = updateExistingUser(ewasteApplication, requestInfo, role, user,
						userMapFromSearch.get(uuid));
			} else {

				user.setUserName(UUID.randomUUID().toString());
				userDetailResponse = createUser(requestInfo, user);
			}
		}
		setuserFields(user, userDetailResponse, requestInfo);
	}

	/**
	 * update existing user
	 * 
	 */
	private UserDetailResponse updateExistingUser(EwasteApplication ewasteApplication, RequestInfo requestInfo,
			Role role, User userFromRequest, User UserFromSearch) {

		UserDetailResponse userDetailResponse;

		userFromRequest.setId(UserFromSearch.getId());
		userFromRequest.setUuid(UserFromSearch.getUuid());
		addUserDefaultFields(ewasteApplication.getTenantId(), role, userFromRequest);

		StringBuilder uri = new StringBuilder(userHost).append(userContextPath).append(userUpdateEndpoint);
		userDetailResponse = userCall(new CreateUserRequest(requestInfo, userFromRequest), uri);
		if (userDetailResponse.getUser().get(0).getUuid() == null) {
			throw new CustomException("INVALID USER RESPONSE", "The user updated has uuid as null");
		}
		return userDetailResponse;
	}

	private UserDetailResponse createUser(RequestInfo requestInfo, User user) {
		UserDetailResponse userDetailResponse;
		StringBuilder uri = new StringBuilder(userHost).append(userContextPath).append(userCreateEndpoint);

		CreateUserRequest userRequest = CreateUserRequest.builder().requestInfo(requestInfo).user(user).build();

		userDetailResponse = userCall(userRequest, uri);

		if (ObjectUtils.isEmpty(userDetailResponse)) {

			throw new CustomException("INVALID USER RESPONSE",
					"The user create has failed for the mobileNumber : " + user.getUserName());

		}
		return userDetailResponse;
	}

	/**
	 * Sets the role,type,active and tenantId for a Citizen
	 * 
	 * @param tenantId TenantId of the ewaste application
	 * @param role     The role of the user set in this case to CITIZEN
	 * @param user     The user whose fields are to be set
	 */
	private void addUserDefaultFields(String tenantId, Role role, User user) {

		user.setActive(true);
		user.setTenantId(tenantId);
		user.setRoles(Collections.singletonList(role));
		user.setType("CITIZEN");
		user.setCreatedDate(null);
		user.setCreatedBy(null);
		user.setLastModifiedDate(null);
		user.setLastModifiedBy(null);
	}

	private Role getCitizenRole() {

		return Role.builder().code("CITIZEN").name("Citizen").build();
	}

	/**
	 * Searches if the user is already created. Search is based on name of user,
	 * uuid and mobileNumber
	 * 
	 * @param user        user which is to be searched
	 * @param requestInfo RequestInfo from the Ewaste Request
	 * @return UserDetailResponse containing the user if present and the
	 *         responseInfo
	 */
	private UserDetailResponse userExists(User user, RequestInfo requestInfo) {

		UserSearchRequest userSearchRequest = getBaseUserSearchRequest(user.getTenantId(), requestInfo);
		userSearchRequest.setMobileNumber(user.getMobileNumber());
		userSearchRequest.setUserType(user.getType());
		userSearchRequest.setName(user.getName());

		StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
		return userCall(userSearchRequest, uri);
	}

	/**
	 * Sets userName for the user as mobileNumber if mobileNumber already assigned
	 * last 10 digits of currentTime is assigned as userName
	 * 
	 * @param user               user whose username has to be assigned
	 * @param listOfMobileNumber list of unique mobileNumbers in the
	 *                           Ewaste Request
	 */
	private void setUserName(User user, Set<String> listOfMobileNumber) {

		if (listOfMobileNumber.contains(user.getMobileNumber())) {
			user.setUserName(user.getMobileNumber());
			// Once mobileNumber is set as userName it is removed from the list
			listOfMobileNumber.remove(user.getMobileNumber());
		} else {
			String username = UUID.randomUUID().toString();
			user.setUserName(username);
		}
	}

	/**
	 * Returns user using user search based on ewaste ApplicationCriteria(user
	 * name,mobileNumber,userName)
	 * 
	 * @param userSearchRequest
	 * @return serDetailResponse containing the user if present and the responseInfo
	 */
	public UserDetailResponse getUser(UserSearchRequest userSearchRequest) {

		StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
		UserDetailResponse userDetailResponse = userCall(userSearchRequest, uri);
		return userDetailResponse;
	}

	/**
	 * Returns UserDetailResponse by calling user service with given uri and object
	 * 
	 * @param userRequest Request object for user service
	 * @param url         The address of the endpoint
	 * @return Response from user service as parsed as userDetailResponse
	 */
	@SuppressWarnings("unchecked")
	private UserDetailResponse userCall(Object userRequest, StringBuilder url) {

		String dobFormat = null;
		if (url.indexOf(userSearchEndpoint) != -1 || url.indexOf(userUpdateEndpoint) != -1)
			dobFormat = "yyyy-MM-dd";
		else if (url.indexOf(userCreateEndpoint) != -1)
			dobFormat = "dd/MM/yyyy";
		try {
			Optional<Object> response = serviceRequestRepository.fetchResult(url, userRequest);

			if (response.isPresent()) {
				LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) response.get();
				parseResponse(responseMap, dobFormat);
				UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
				return userDetailResponse;
			} else {
				return new UserDetailResponse();
			}
		}
		// Which Exception to throw?
		catch (IllegalArgumentException e) {
			throw new CustomException("IllegalArgumentException", "ObjectMapper not able to convertValue in userCall");
		}
	}

	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responeMap LinkedHashMap got from user api response
	 * @param dobFormat  dob format (required because dob is returned in different
	 *                   format's in search and create response in user service)
	 */
	@SuppressWarnings("unchecked")
	private void parseResponse(LinkedHashMap<String, Object> responeMap, String dobFormat) {

		List<LinkedHashMap<String, Object>> users = (List<LinkedHashMap<String, Object>>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";

		if (null != users) {

			users.forEach(map -> {

				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
			});
		}
	}

	/**
	 * Converts date to long
	 * 
	 * @param date   date to be parsed
	 * @param format Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}

	/**
	 * Sets user fields (so that the user table can be linked to user table)
	 * 
	 * @param user               user in the ewaste detail whose user is created
	 * @param userDetailResponse userDetailResponse from the user Service
	 *                           corresponding to the given user
	 */
	private void setuserFields(User user, UserDetailResponse userDetailResponse, RequestInfo requestInfo) {

		user.setUuid(userDetailResponse.getUser().get(0).getUuid());
		user.setId(userDetailResponse.getUser().get(0).getId());
		user.setUserName((userDetailResponse.getUser().get(0).getUserName()));
		user.setCreatedBy(requestInfo.getUserInfo().getUuid());
		user.setCreatedDate(System.currentTimeMillis());
		user.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
		user.setLastModifiedDate(System.currentTimeMillis());
		user.setActive(userDetailResponse.getUser().get(0).getActive());
	}

	/**
	 * Updates user if present else creates new user
	 * 
	 * @param request Ewaste Request received from update
	 */

	/**
	 * provides a user search request with basic mandatory parameters
	 * 
	 * @param tenantId
	 * @param requestInfo
	 * @return
	 */
	public UserSearchRequest getBaseUserSearchRequest(String tenantId, RequestInfo requestInfo) {

		return UserSearchRequest.builder().requestInfo(requestInfo).userType("CITIZEN").tenantId(tenantId).active(true)
				.build();
	}

	private UserDetailResponse searchByUserName(String userName, String tenantId) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setUserType("CITIZEN");
		userSearchRequest.setUserName(userName);
		userSearchRequest.setTenantId(tenantId);
		return getUser(userSearchRequest);
	}

	private String getStateLevelTenant(String tenantId) {
		return tenantId.split("\\.")[0];
	}

	private UserDetailResponse searchedSingleUserExists(User user, RequestInfo requestInfo) {

		UserSearchRequest userSearchRequest = getBaseUserSearchRequest(user.getTenantId(), requestInfo);
		userSearchRequest.setUserType(user.getType());
		Set<String> uuids = new HashSet<String>();
		uuids.add(user.getUuid());
		userSearchRequest.setUuid(uuids);

		StringBuilder uri = new StringBuilder(userHost).append(userSearchEndpoint);
		return userCall(userSearchRequest, uri);
	}

}
