# OmegaT plugin for Oxford Dictionary API search

This is implementation of Oxford online dictionary search plugin for OmegaT,
that utilize Oxford dictionaries(OD) API

Current status is `ALPHA`.

## Prereuisite

This plugin requires a modification of OmegaT Core dicitionary API.
Please check ticket and pull-request.

- https://sourceforge.net/p/omegat/feature-requests/1597/
- https://github.com/omegat-org/omegat/pull/178

You should prepare app id and app key from [Oxford dictionaries API site](https://developer.oxforddictionaries.com/).

## Screenshot

When OmegaT accept an enhancement and loading plugin correctly, you can see query results like

![omegat-oxford-dictionary-example](https://raw.githubusercontent.com/miurahr/omegat-oxford-dictionaries/main/docs/images/main_screenshot.png)

### Configuration

You are recommended to disable an automate search of dictionaries.
Oxford Dictionaries API costs for each word searches. You can easily consumes thousands of queeries.

![dictionary configuration recommend](https://raw.githubusercontent.com/miurahr/omegat-oxford-dictionaries/main/docs/images/gui_preference_dictionary_options.png)

To use the plugin, you should enable plugin and set credentials in preferences -> plugins.

![dictionary configuration recommend](https://raw.githubusercontent.com/miurahr/omegat-oxford-dictionaries/main/docs/images/gui_preferences_plugins_oxford.png)

