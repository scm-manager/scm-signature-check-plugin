/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.scm.signature.check.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware("TrainerRed")
class SignatureConfigResourceTest {

  @Mock
  private SignatureConfigService signatureConfigService;

  @Mock
  private NamespaceManager namespaceManager;

  @Mock
  private RepositoryManager repositoryManager;

  private RestDispatcher dispatcher;

  private final String domainForLinks = "https://scm-manager.org/scm/api/";

  private final String basePath = "/v2/signature-check";

  private final String globalConfigPath = "/global-config";

  private final String expectedGlobalConfigLink = domainForLinks + basePath.substring(1) + globalConfigPath;
  private final String expectedBaseLinkForNamespaceAndRepo = domainForLinks + basePath.substring(1) + "/";

  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void init() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create(domainForLinks));

    SignatureConfigResource signatureConfigResource = new SignatureConfigResource(
      signatureConfigService,
      namespaceManager,
      repositoryManager,
      scmPathInfoStore
    );

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(signatureConfigResource);
  }

  @Nested
  class RepoConfig {
    private final Repository repository = RepositoryTestData.create42Puzzle();

    @Test
    void shouldThrowUnauthorizedForGetBecauseOfMissingPermission() throws URISyntaxException {
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);
      JsonMockHttpResponse response = invokeGetRepoConfig(repository.getNamespace(), repository.getName());

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(signatureConfigService);
    }


    @Test
    void shouldThrowNotFoundForGetBecauseNamespaceDoesNotExist() throws URISyntaxException {
      String unknownNamespace = "unknownNamespace";
      String unknownName = "unknownName";
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);

      JsonMockHttpResponse response = invokeGetRepoConfig(unknownNamespace, unknownName);

      assertThat(response.getStatus()).isEqualTo(404);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    @SubjectAware(permissions = "repository:signatureCheck:*")
    void shouldReturnRepoConfigDto() throws URISyntaxException {
      RepositorySignatureConfig expectedConfig = new RepositorySignatureConfig();
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);
      when(signatureConfigService.getRepoConfig(repository)).thenReturn(expectedConfig);

      JsonMockHttpResponse response = invokeGetRepoConfig(repository.getNamespace(), repository.getName());

      assertThat(response.getStatus()).isEqualTo(200);

      JsonNode root = response.getContentAsJson();
      assertThat(root.get("overwriteParentConfig").asBoolean()).isTrue();
      assertThat(root.get("enabled").asBoolean()).isTrue();
      assertThat(root.get("verificationType").asText()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE.name());

      assertThat(root.get("protectedBranches").isArray()).isTrue();
      assertThat(root.get("protectedBranches").get(0).asText()).isEqualTo("develop");
      assertThat(root.get("protectedBranches").get(1).asText()).isEqualTo("main");

      assertThat(root.get("_links").get("self").get("href").asText()).isEqualTo(expectedBaseLinkForNamespaceAndRepo + repository.getNamespace() + "/" + repository.getName());
      assertThat(root.get("_links").get("update").get("href").asText()).isEqualTo(expectedBaseLinkForNamespaceAndRepo + repository.getNamespace() + "/" + repository.getName());

      verify(signatureConfigService).getRepoConfig(repository);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesAreNull() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetRepoConfig(
        repository.getNamespace(),
        repository.getName(),
        new RepositorySignatureConfigDto(
          true,
          true,
          null,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesContainsNullElement() throws URISyntaxException, JsonProcessingException {
      List<String> protectedBranches = new ArrayList<>();
      protectedBranches.add(null);

      JsonMockHttpResponse response = invokeSetRepoConfig(
        repository.getNamespace(),
        repository.getName(),
        new RepositorySignatureConfigDto(
          true,
          true,
          protectedBranches,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesContainsEmptyElement() throws URISyntaxException, JsonProcessingException {
      List<String> protectedBranches = new ArrayList<>();
      protectedBranches.add("");

      JsonMockHttpResponse response = invokeSetRepoConfig(
        repository.getNamespace(),
        repository.getName(),
        new RepositorySignatureConfigDto(
          true,
          true,
          protectedBranches,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseVerificationTypeIsNull() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetRepoConfig(
        repository.getNamespace(),
        repository.getName(),
        new RepositorySignatureConfigDto(
          true,
          true,
          List.of("main", "develop"),
          null
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldThrowUnauthorizedForSetBecauseOfMissingPermission() throws URISyntaxException, JsonProcessingException {
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);
      JsonMockHttpResponse response = invokeSetRepoConfig(repository.getNamespace(), repository.getName(), new RepositorySignatureConfigDto(
        true,
        true,
        List.of("main"),
        GpgVerificationType.ANY_SIGNATURE
      ));

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldThrowNotFoundForSetBecauseRepoDoesNotExist() throws URISyntaxException, JsonProcessingException {
      String unknownNamespace = "unknownNamespace";
      String unknownName = "unknownName";
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(null);

      JsonMockHttpResponse response = invokeSetRepoConfig(unknownNamespace, unknownName, new RepositorySignatureConfigDto(
        true,
        true,
        List.of("main", "develop"),
        GpgVerificationType.SCM_USER_SIGNATURE
      ));

      assertThat(response.getStatus()).isEqualTo(404);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    @SubjectAware(permissions = "repository:signatureCheck:*")
    void shouldSetRepoConfig() throws URISyntaxException, JsonProcessingException {
      when(repositoryManager.get(any(NamespaceAndName.class))).thenReturn(repository);
      RepositorySignatureConfigDto expectedConfigDto = new RepositorySignatureConfigDto(
        true,
        true,
        List.of("main", "develop"),
        GpgVerificationType.SCM_USER_SIGNATURE
      );

      RepositorySignatureConfig expectedConfig = new RepositorySignatureConfig();
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));

      JsonMockHttpResponse response = invokeSetRepoConfig(repository.getNamespace(), repository.getName(), expectedConfigDto);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(signatureConfigService).setRepoConfig(
        repository,
        expectedConfig
      );
    }

    private JsonMockHttpResponse invokeGetRepoConfig(String namespace, String name) throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get(basePath + "/" + namespace + "/" + name);
      JsonMockHttpResponse response = new JsonMockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }

    private JsonMockHttpResponse invokeSetRepoConfig(String namespace, String name, RepositorySignatureConfigDto dto) throws URISyntaxException, JsonProcessingException {
      MockHttpRequest request = MockHttpRequest
        .put(basePath + "/" + namespace + "/" + name)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsBytes(dto));

      JsonMockHttpResponse response = new JsonMockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }
  }

  @Nested
  class NamespaceConfig {

    final Namespace namespace = new Namespace("scmadmin-space");

    @Test
    void shouldThrowUnauthorizedForGetBecauseOfMissingPermission() throws URISyntaxException {
      when(namespaceManager.get(namespace.getNamespace())).thenReturn(Optional.of(namespace));
      JsonMockHttpResponse response = invokeGetNamespaceConfig(namespace.getNamespace());

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldThrowUnauthorizedForSetBecauseOfMissingPermission() throws URISyntaxException, JsonProcessingException {
      when(namespaceManager.get(namespace.getNamespace())).thenReturn(Optional.of(namespace));
      JsonMockHttpResponse response = invokeSetNamespaceConfig(namespace.getNamespace(), new NamespaceSignatureConfigDto(
        true,
        true,
        true,
        List.of("develop"),
        GpgVerificationType.ANY_SIGNATURE
      ));

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldThrowNotFoundForGetBecauseNamespaceDoesNotExist() throws URISyntaxException {
      String unknownNamespace = "unknownNamespace";
      when(namespaceManager.get(unknownNamespace)).thenReturn(Optional.empty());

      JsonMockHttpResponse response = invokeGetNamespaceConfig("unknownNamespace");

      assertThat(response.getStatus()).isEqualTo(404);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    @SubjectAware(permissions = "namespace:signatureCheck:*")
    void shouldReturnNamespaceConfigDto() throws URISyntaxException {
      NamespaceSignatureConfig expectedConfig = new NamespaceSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));
      when(namespaceManager.get(namespace.getNamespace())).thenReturn(Optional.of(namespace));
      when(signatureConfigService.getNamespaceConfig(any(Namespace.class))).thenReturn(expectedConfig);

      JsonMockHttpResponse response = invokeGetNamespaceConfig(namespace.getNamespace());

      assertThat(response.getStatus()).isEqualTo(200);

      JsonNode root = response.getContentAsJson();
      assertThat(root.get("childrenConfigDisabled").asBoolean()).isTrue();
      assertThat(root.get("overwriteParentConfig").asBoolean()).isTrue();
      assertThat(root.get("enabled").asBoolean()).isTrue();
      assertThat(root.get("verificationType").asText()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE.name());

      assertThat(root.get("protectedBranches").isArray()).isTrue();
      assertThat(root.get("protectedBranches").get(0).asText()).isEqualTo("develop");
      assertThat(root.get("protectedBranches").get(1).asText()).isEqualTo("main");

      assertThat(root.get("_links").get("self").get("href").asText()).isEqualTo(expectedBaseLinkForNamespaceAndRepo + namespace.getNamespace());
      assertThat(root.get("_links").get("update").get("href").asText()).isEqualTo(expectedBaseLinkForNamespaceAndRepo + namespace.getNamespace());

      verify(signatureConfigService).getNamespaceConfig(namespace);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesAreNull() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetNamespaceConfig(
        namespace.getNamespace(),
        new NamespaceSignatureConfigDto(
          true,
          true,
          true,
          null,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesContainsNullElement() throws URISyntaxException, JsonProcessingException {
      List<String> protectedBranches = new ArrayList<>();
      protectedBranches.add(null);

      JsonMockHttpResponse response = invokeSetNamespaceConfig(
        namespace.getNamespace(),
        new NamespaceSignatureConfigDto(
          true,
          true,
          true,
          protectedBranches,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesContainsEmptyElement() throws URISyntaxException, JsonProcessingException {
      List<String> protectedBranches = new ArrayList<>();
      protectedBranches.add("");

      JsonMockHttpResponse response = invokeSetNamespaceConfig(
        namespace.getNamespace(),
        new NamespaceSignatureConfigDto(
          true,
          true,
          true,
          protectedBranches,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseVerificationTypeIsNull() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetNamespaceConfig(
        namespace.getNamespace(),
        new NamespaceSignatureConfigDto(
          true,
          true,
          true,
          List.of("main", "develop"),
          null
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldThrowNotFoundForSetBecauseNamespaceDoesNotExist() throws URISyntaxException, JsonProcessingException {
      String unknownNamespace = "unknownNamespace";
      when(namespaceManager.get(unknownNamespace)).thenReturn(Optional.empty());

      JsonMockHttpResponse response = invokeSetNamespaceConfig("unknownNamespace", new NamespaceSignatureConfigDto(
        true,
        true,
        true,
        List.of("main", "develop"),
        GpgVerificationType.SCM_USER_SIGNATURE
      ));

      assertThat(response.getStatus()).isEqualTo(404);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    @SubjectAware(permissions = "namespace:signatureCheck:*")
    void shouldSetNamespaceConfig() throws URISyntaxException, JsonProcessingException {
      when(namespaceManager.get(namespace.getNamespace())).thenReturn(Optional.of(namespace));
      NamespaceSignatureConfigDto expectedConfigDto = new NamespaceSignatureConfigDto(
        true,
        true,
        true,
        List.of("main", "develop"),
        GpgVerificationType.SCM_USER_SIGNATURE
      );

      NamespaceSignatureConfig expectedConfig = new NamespaceSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setOverwriteParentConfig(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));

      JsonMockHttpResponse response = invokeSetNamespaceConfig(namespace.getNamespace(), expectedConfigDto);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(signatureConfigService).setNamespaceConfig(
        namespace,
        expectedConfig
      );
    }

    private JsonMockHttpResponse invokeGetNamespaceConfig(String namespace) throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get(basePath + "/" + namespace);
      JsonMockHttpResponse response = new JsonMockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }

    private JsonMockHttpResponse invokeSetNamespaceConfig(String namespace, NamespaceSignatureConfigDto dto) throws URISyntaxException, JsonProcessingException {
      MockHttpRequest request = MockHttpRequest
        .put(basePath + "/" + namespace)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsBytes(dto));

      JsonMockHttpResponse response = new JsonMockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }
  }

  @Nested
  class GlobalConfig {

    @Test
    void shouldThrowUnauthorizedForGetBecauseOfMissingPermission() throws URISyntaxException {
      JsonMockHttpResponse response = invokeGetGlobalConfig();

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldThrowUnauthorizedForSetBecauseOfMissingPermission() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetGlobalConfig(new GlobalSignatureConfigDto(
        true,
        true,
        List.of("develop"),
        GpgVerificationType.ANY_SIGNATURE
      ));

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    @SubjectAware(permissions = "configuration:read:signatureCheck")
    void shouldReturnGlobalConfigDto() throws URISyntaxException {
      GlobalSignatureConfig expectedConfig = new GlobalSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));
      when(signatureConfigService.getGlobalConfig()).thenReturn(expectedConfig);

      JsonMockHttpResponse response = invokeGetGlobalConfig();

      assertThat(response.getStatus()).isEqualTo(200);

      JsonNode root = response.getContentAsJson();
      assertThat(root.get("childrenConfigDisabled").asBoolean()).isTrue();
      assertThat(root.get("enabled").asBoolean()).isTrue();
      assertThat(root.get("verificationType").asText()).isEqualTo(GpgVerificationType.SCM_USER_SIGNATURE.name());

      assertThat(root.get("protectedBranches").isArray()).isTrue();
      assertThat(root.get("protectedBranches").get(0).asText()).isEqualTo("develop");
      assertThat(root.get("protectedBranches").get(1).asText()).isEqualTo("main");

      assertThat(root.get("_links").get("self").get("href").asText()).isEqualTo(expectedGlobalConfigLink);
      assertThat(root.get("_links").get("update").get("href").asText()).isEqualTo(expectedGlobalConfigLink);

      verify(signatureConfigService).getGlobalConfig();
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesAreNull() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetGlobalConfig(
        new GlobalSignatureConfigDto(
          true,
          true,
          null,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesContainsNullElement() throws URISyntaxException, JsonProcessingException {
      List<String> protectedBranches = new ArrayList<>();
      protectedBranches.add(null);

      JsonMockHttpResponse response = invokeSetGlobalConfig(
        new GlobalSignatureConfigDto(
          true,
          true,
          protectedBranches,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseProtectedBranchesContainsEmptyElement() throws URISyntaxException, JsonProcessingException {
      List<String> protectedBranches = new ArrayList<>();
      protectedBranches.add("");

      JsonMockHttpResponse response = invokeSetGlobalConfig(
        new GlobalSignatureConfigDto(
          true,
          true,
          protectedBranches,
          GpgVerificationType.SCM_USER_SIGNATURE
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    void shouldReturnBadRequestBecauseVerificationTypeIsNull() throws URISyntaxException, JsonProcessingException {
      JsonMockHttpResponse response = invokeSetGlobalConfig(
        new GlobalSignatureConfigDto(
          true,
          true,
          List.of("main", "develop"),
          null
        ));

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(signatureConfigService);
    }

    @Test
    @SubjectAware(permissions = "configuration:write:signatureCheck")
    void shouldSetGlobalConfig() throws URISyntaxException, JsonProcessingException {
      GlobalSignatureConfigDto configDto = new GlobalSignatureConfigDto(
        true,
        true,
        List.of("main", "develop"),
        GpgVerificationType.SCM_USER_SIGNATURE
      );

      GlobalSignatureConfig expectedConfig = new GlobalSignatureConfig();
      expectedConfig.setChildrenConfigDisabled(true);
      expectedConfig.setEnabled(true);
      expectedConfig.setVerificationType(GpgVerificationType.SCM_USER_SIGNATURE);
      expectedConfig.setProtectedBranches(List.of("develop", "main"));

      JsonMockHttpResponse response = invokeSetGlobalConfig(configDto);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(signatureConfigService).setGlobalConfig(
        expectedConfig
      );
    }

    private JsonMockHttpResponse invokeGetGlobalConfig() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get(basePath + globalConfigPath);
      JsonMockHttpResponse response = new JsonMockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }

    private JsonMockHttpResponse invokeSetGlobalConfig(GlobalSignatureConfigDto dto) throws URISyntaxException, JsonProcessingException {
      MockHttpRequest request = MockHttpRequest
        .put(basePath + globalConfigPath)
        .contentType(MediaType.APPLICATION_JSON)
        .content(mapper.writeValueAsBytes(dto));

      JsonMockHttpResponse response = new JsonMockHttpResponse();

      dispatcher.invoke(request, response);
      return response;
    }
  }
}
