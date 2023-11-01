---
title: Usage
---

By default, changesets can be pushed to a repository without a signature.
With the Signature Check plugin, this can be prevented so that only signed changesets can be pushed.

The specific behavior of the signature check can be set at global, namespace and repository level.
Initially, only the global setting is active.
However, the namespace settings can explicitly override the global setting.
In this case, the namespace setting is active instead of the global setting.
Similarly, the repository setting can overwrite the global and namespace setting.
When overwriting, the settings are not merged, instead only the now active setting is applied.

To prevent overwriting, you can explicitly disable all repository settings in the namespace setting.
This will disable the settings for all repositories that belong to that namespace.
This applies even if the repository setting overrides the namespace setting.
Similarly, you can explicitly disable all namespace and repository settings in the global setting.
This would make only the global setting active.

## Global Settings

In order to open the global setting, one should open the `Administration` page via the primary navigation.
And then navigate via the secondary navigation to `Settings` and the subitem `Signature Check`.
If the corresponding options are not available in the navigation, then the required [permissions](#Permissions) are missing.

In the global setting, one can first specify via checkbox whether the namespace and repository settings should be explicitly disabled.
Next, you can use a checkbox to enable the feature to check every changeset for a valid signature.
As soon as the check is activated, further options become visible to the user.

This allows the user to specify via chip input which branches should be protected from unsigned changesets during a push.
If the chip input is empty, then all branches are protected.
Otherwise, only the specified branches are protected.

Next, the user can specify via radio buttons which gpg signatures are allowed.
Either any signatures can be allowed or it must be the signature of an SCM manager user.
If any signatures are allowed and the used public key of a signature is known to the SCM manager,
then the signature must also match the content of the respective changeset, otherwise the push with this changeset will be rejected.
If the public key is not known, it is not possible to check whether the signature is correct.
In this case it is sufficient if the changeset has a signature.
If only signatures of SCM Manager users are allowed, then a check is made for each signature,
whether the public key belongs to a user and whether the signature matches the changeset.
Otherwise, the push would be rejected with the respective changeset.

Finally, the user has to save his settings with the `Save` button.

## Namespace Settings

To open the namespace setting you first have to go to the `Repositories` page via the primary navigation.
Here you can see an overview of all namespaces and the repositories they contain.
Next to each namespace there is a button to open the page with the settings for that namespace.
On this page you can open the `Signature Check` settings via the secondary navigation.
If the corresponding options are not available in the navigation, then the required [permissions](#Permissions) are missing or the namespace setting has been disabled globally.

In the namespace setting, you can use a checkbox to first specify whether the global setting should be overwritten.
This would only affect the respective namespace.
As soon as the global setting is set to be overwritten, further setting options are shown to the user.
The next setting allows the user to specify via checkbox whether the repository settings should be explicitly disabled.
This would only affect the repositories that are located in the respective namespace.
The subsequent setting options are analogical to the global setting.

## Repository Settings

To open the repository setting you first have to go to the `Repositories` page via the primary navigation.
Here you can see an overview of all namespaces and the repositories they contain.
Then you have to click on the repository you want to configure, this will open the page of the repository.
With the secondary navigation and the subitem `Signature Check` in the tab `Settings` the repository setting can be opened.
If the corresponding options are not present in the navigation, then the required [permissions](#Permissions) are missing or the repository setting has been disabled globally or by namespace.

First, the user can use a checkbox to specify whether the settings from this repository should override the global and respective namespace setting.
In case of an override, further setting options are displayed analogical to the global setting.

## Permissions

To read or change the settings it needs the respective permissions.
For the global setting at least the `Administer permissions` and the `Modifiy signature check configuration` permissions are required.
For the namespace setting, the `Read permissions on namespaces` permission must exist globally and the `configurate signature check` permission must exist for the respective namespace.
For the repository setting, the same permissions as for the namespace setting must be assigned at namespace or repository level.
