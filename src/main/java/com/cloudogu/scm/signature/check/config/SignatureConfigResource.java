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

import com.cloudogu.scm.signature.check.SignatureCheckPermissions;
import de.otto.edison.hal.Links;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import sonia.scm.NotFoundException;
import sonia.scm.api.v2.resources.ErrorDto;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.web.VndMediaType;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Path("v2/signature-check")
public class SignatureConfigResource {

  private final SignatureConfigService signatureConfigService;
  private final NamespaceManager namespaceManager;
  private final RepositoryManager repositoryManager;
  private final ScmPathInfoStore scmPathInfoStore;

  @Inject
  public SignatureConfigResource(SignatureConfigService signatureConfigService, NamespaceManager namespaceManager, RepositoryManager repositoryManager, ScmPathInfoStore scmPathInfoStore) {
    this.signatureConfigService = signatureConfigService;
    this.namespaceManager = namespaceManager;
    this.repositoryManager = repositoryManager;
    this.scmPathInfoStore = scmPathInfoStore;
  }

  @GET
  @Path("/global-config")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Gets the global signature check config",
    description = "Gets the global signature check config",
    tags = "Signature Check Plugin",
    operationId = "get_global_config"
  )
  @ApiResponse(responseCode = "200", description = "Ok")
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  @ApiResponse(responseCode = "403", description = "Missing Permissions")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getGlobalConfig() {
    ConfigurationPermissions.read("signatureCheck").check();
    GlobalSignatureConfig config = signatureConfigService.getGlobalConfig();
    return Response.ok().entity(mapConfigToDto(config)).build();
  }

  private GlobalSignatureConfigDto mapConfigToDto(GlobalSignatureConfig config) {
    GlobalSignatureConfigDto dto = new GlobalSignatureConfigDto(
      config.isChildrenConfigDisabled(),
      config.isEnabled(),
      config.getProtectedBranches(),
      config.getVerificationType()
    );

    Links.Builder linksBuilder = linkingTo().self(globalConfigSelfLink());
    linksBuilder.single(link("update", globalConfigUpdateLink()));
    dto.add(linksBuilder.build());

    return dto;
  }

  private String globalConfigSelfLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), SignatureConfigResource.class);
    return linkBuilder.method("getGlobalConfig").parameters().href();
  }

  private String globalConfigUpdateLink() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), SignatureConfigResource.class);
    return linkBuilder.method("setGlobalConfig").parameters().href();
  }

  @PUT
  @Path("/global-config")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Sets the global signature check config",
    description = "Sets the global signature check config",
    tags = "Signature Check Plugin",
    operationId = "set_global_config"
  )
  @ApiResponse(responseCode = "204", description = "Ok")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  @ApiResponse(responseCode = "403", description = "Missing Permissions")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response setGlobalConfig(@Valid GlobalSignatureConfigDto configDto) {
    ConfigurationPermissions.write("signatureCheck").check();
    signatureConfigService.setGlobalConfig(mapDtoToConfig(configDto));
    return Response.noContent().build();
  }

  private GlobalSignatureConfig mapDtoToConfig(GlobalSignatureConfigDto dto) {
    GlobalSignatureConfig config = new GlobalSignatureConfig();
    config.setChildrenConfigDisabled(dto.isChildrenConfigDisabled());
    config.setEnabled(dto.isEnabled());
    config.setProtectedBranches(dto.getProtectedBranches());
    config.setVerificationType(dto.getVerificationType());

    return config;
  }

  @GET
  @Path("/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Gets the signature check config of a namespace",
    description = "Gets signature check config of a namespace",
    tags = "Signature Check Plugin",
    operationId = "get_namespace_config"
  )
  @ApiResponse(responseCode = "200", description = "Ok")
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  @ApiResponse(responseCode = "403", description = "Missing Permissions")
  @ApiResponse(responseCode = "404", description = "Namespace not found")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getNamespaceConfig(@PathParam("namespace") String namespace) {
    Namespace foundNamespace = namespaceManager
      .get(namespace)
      .orElseThrow(
        () -> new NotFoundException(Namespace.class, namespace)
      );

    SignatureCheckPermissions.checkNamespace(namespace);
    NamespaceSignatureConfig config = signatureConfigService.getNamespaceConfig(foundNamespace);
    return Response.ok().entity(mapConfigToDto(config, namespace)).build();
  }

  private NamespaceSignatureConfigDto mapConfigToDto(NamespaceSignatureConfig config, String namespace) {
    NamespaceSignatureConfigDto dto = new NamespaceSignatureConfigDto(
      config.isChildrenConfigDisabled(),
      config.isOverwriteParentConfig(),
      config.isEnabled(),
      config.getProtectedBranches(),
      config.getVerificationType()
    );

    Links.Builder linksBuilder = linkingTo().self(namespaceConfigSelfLink(namespace));
    linksBuilder.single(link("update", namespaceConfigUpdateLink(namespace)));
    dto.add(linksBuilder.build());

    return dto;
  }

  private String namespaceConfigSelfLink(String namespace) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), SignatureConfigResource.class);
    return linkBuilder.method("getNamespaceConfig").parameters(namespace).href();
  }

  private String namespaceConfigUpdateLink(String namespace) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), SignatureConfigResource.class);
    return linkBuilder.method("setNamespaceConfig").parameters(namespace).href();
  }

  @PUT
  @Path("/{namespace}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Sets the signature check config for a namespace",
    description = "Sets the signature check config for a namespace",
    tags = "Signature Check Plugin",
    operationId = "set_namespace_config"
  )
  @ApiResponse(responseCode = "204", description = "Ok")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  @ApiResponse(responseCode = "403", description = "Missing Permissions")
  @ApiResponse(responseCode = "404", description = "Namespace not found")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response setNamespaceConfig(@PathParam("namespace") String namespace, @Valid NamespaceSignatureConfigDto configDto) {
    Namespace foundNamespace = namespaceManager
      .get(namespace)
      .orElseThrow(
        () -> new NotFoundException(Namespace.class, namespace)
      );

    SignatureCheckPermissions.checkNamespace(namespace);
    signatureConfigService.setNamespaceConfig(foundNamespace, mapDtoToConfig(configDto));

    return Response.noContent().build();
  }

  private NamespaceSignatureConfig mapDtoToConfig(NamespaceSignatureConfigDto dto) {
    NamespaceSignatureConfig config = new NamespaceSignatureConfig();
    config.setChildrenConfigDisabled(dto.isChildrenConfigDisabled());
    config.setOverwriteParentConfig(dto.isOverwriteParentConfig());
    config.setEnabled(dto.isEnabled());
    config.setProtectedBranches(dto.getProtectedBranches());
    config.setVerificationType(dto.getVerificationType());

    return config;
  }

  @GET
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Gets the signature check config of a repository",
    description = "Gets signature check config of a repository",
    tags = "Signature Check Plugin",
    operationId = "get_repo_config"
  )
  @ApiResponse(responseCode = "200", description = "Ok")
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  @ApiResponse(responseCode = "403", description = "Missing Permissions")
  @ApiResponse(responseCode = "404", description = "Repository not found")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response getRepoConfig(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository foundRepository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if(foundRepository == null) {
      throw new NotFoundException(Repository.class, namespace + "/" + name);
    }

    RepositoryPermissions.custom("signatureCheck", foundRepository.getId()).check();
    RepositorySignatureConfig config = signatureConfigService.getRepoConfig(foundRepository);

    return Response.ok().entity(mapConfigToDto(config, foundRepository)).build();
  }

  private RepositorySignatureConfigDto mapConfigToDto(RepositorySignatureConfig config, Repository repository) {
    RepositorySignatureConfigDto dto = new RepositorySignatureConfigDto(
      config.isOverwriteParentConfig(),
      config.isEnabled(),
      config.getProtectedBranches(),
      config.getVerificationType()
    );

    Links.Builder linksBuilder = linkingTo().self(repoConfigSelfLink(repository));
    linksBuilder.single(link("update", repoConfigUpdateLink(repository)));
    dto.add(linksBuilder.build());

    return dto;
  }

  private String repoConfigSelfLink(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), SignatureConfigResource.class);
    return linkBuilder.method("getRepoConfig").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String repoConfigUpdateLink(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), SignatureConfigResource.class);
    return linkBuilder.method("setRepoConfig").parameters(repository.getNamespace(), repository.getName()).href();
  }

  @PUT
  @Path("/{namespace}/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Operation(
    summary = "Sets the signature check config for a repository",
    description = "Sets the signature check config for a repository",
    tags = "Signature Check Plugin",
    operationId = "set_repo_config"
  )
  @ApiResponse(responseCode = "204", description = "Ok")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "401", description = "Not authenticated")
  @ApiResponse(responseCode = "403", description = "Missing Permissions")
  @ApiResponse(responseCode = "404", description = "Repository not found")
  @ApiResponse(
    responseCode = "500",
    description = "internal server error",
    content = @Content(
      mediaType = VndMediaType.ERROR_TYPE,
      schema = @Schema(implementation = ErrorDto.class)
    )
  )
  public Response setRepoConfig(@PathParam("namespace") String namespace,
                                @PathParam("name") String name,
                                @Valid RepositorySignatureConfigDto configDto) {
    Repository foundRepository = repositoryManager.get(new NamespaceAndName(namespace, name));
    if(foundRepository == null) {
      throw new NotFoundException(Repository.class, namespace + "/" + name);
    }

    RepositoryPermissions.custom("signatureCheck", foundRepository.getId()).check();
    signatureConfigService.setRepoConfig(foundRepository, mapDtoToConfig(configDto));

    return Response.noContent().build();
  }

  private RepositorySignatureConfig mapDtoToConfig(RepositorySignatureConfigDto dto) {
    RepositorySignatureConfig config = new RepositorySignatureConfig();
    config.setOverwriteParentConfig(dto.isOverwriteParentConfig());
    config.setEnabled(dto.isEnabled());
    config.setProtectedBranches(dto.getProtectedBranches());
    config.setVerificationType(dto.getVerificationType());

    return config;
  }
}
