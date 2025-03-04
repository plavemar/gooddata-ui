---
title: Dashboard Plugin API
sidebar_label: Dashboard Plugin API
copyright: (C) 2007-2021 GoodData Corporation
id: version-8.7.0-dashboard_plugins_api
original_id: dashboard_plugins_api
---

Your plugin can take advantage of several categories of APIs that are outlined in this article.

All the public and most of the alpha-level APIs have detailed documentation in form of TSDoc. We generate the API
doc website from these comments. You can find the API reference [here](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.html).

## Plugin contract

The plugin contract is the first API that you will come in touch with. The contract defines properties that you need
to fill in and functions to implement, including the functions that allow you to specify code
to run on load, during registration, and after unload.

A new plugin that is bootstrapped using the [Plugin Development Toolkit](18_dashboard_plugins.md#getting-started) sets up
all the contract essentials and provides a template implementation of some of the functions by subclassing
the `DashboardPluginV1`.

You can modify your subclass as needed. You can even add a plugin-specific internal state into this class, if needed.

### onPluginLoaded

When implemented, this function is called right after your plugin is loaded and is about to be used on a dashboard.
At this point, your code receives information about the context in which it will operate. Most notably:

-  The Analytical Backend that is currently in use
-  The workspace that the user works with
-  The reference of the dashboard that is about to be loaded
-  The Analytical Backend setup authenticated and ready to use in case your plugin needs to read additional data from the backend

This function may return a promise. In that case, the dashboard loader waits until the promise is resolved and
then proceeds. The loader does not impose any timeout on the `onPluginLoaded` call. If you make calls over the network,
a good practice is to consider potential failures and include timeouts and necessary fallbacks.

**NOTE:** If your implementation of this function fails with an exception, the loader excludes your plugin from further processing.

#### Parameterization

To allow a plugin to be reused across dashboards, you can make each link between a dashboard and your plugin specify
additional parameters. These parameters can then be used to customize the behavior of the plugin for
a particular dashboard.

Both the GoodData platform and GoodData.CN treat parameters opaquely. Only soft-size limits of 2,048 bytes for
the length of the parameter string are imposed. Otherwise, the parameters are fully under your control and responsibility.

If the parameters are specified on the link between a dashboard and your plugin, the loader sends them as the
second argument to the `onPluginLoad` function.

### register

This is a mandatory function that your plugin must implement. This function is called after the plugin is loaded. 
In this function, you then can register customizations to the dashboard.

Your function is always called with the following parameters:

-  Dashboard context

   Similar to `onPluginLoaded`, the context contains essential information describing the backend, workspace, and
   dashboard.

-  Customization API

   The customization API allows your code to add new content to a dashboard. Calls to these APIs are only valid
   during the registration phase. If you hold onto the received APIs and attempt to make changes later on, they
   will have no effect and will result in warnings in the browser console.

   For more information, see [Customization API](18_dashboard_plugins_api.md#customization-api).

-  Event handler API

   The event handler API allows your code to add or remove your own custom event handlers for different events
   happening on the dashboard. If needed, you can hold onto the event handler API and use it to add or remove handlers
   at any time during the plugin lifecycle.

   For more information, see [Event handler API](18_dashboard_plugins_api.md#event-handler-api).

### onPluginUnload

When implemented, this function is called right before the dashboard that uses your plugin is unmounted. 
In this function, your code can do additional teardown and cleanup specific to your plugin.

Your function may be asynchronous and return a promise. At this point, the dashboard loader does not wait for the returned
promise to resolve.

## Customization API

The customization API is an entry point to all the customization and enhancement capabilities that your code can take advantage of. The
API is described in detail in the [API reference](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.idashboardcustomizer.html).

### Customize rendering of insights

Call the `insightWidgets()` method on the customization API to get to the API through which you can customize
how the [insight widgets](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.idashboardinsightcustomizer.html)
on your dashboard will be rendered.

* To **render data for one or more insights** using your own custom visualizations, the insight widget customization API provides the following methods:
   * `withCustomProvider()`
         
      When calling this method, you can register a function that will be called by the Dashboard component every time it
      wants to render an insight widget. The function will receive a widget definition and the insight to be rendered. This function
      is expected to return a React component to use for rendering that widget. If the function does not return
      a custom component, the Dashboard component falls back and uses the built-in component.

      Your function must determine that a particular insight widget should be rendered using
      a custom component and then return the custom component. How your function determines this is up to you.

   * `withTag()`
         
      This is a convenience method on top of the `withCustomProvider` method. To identify insight widgets to render
      using custom components, use tags. You can assign arbitrary tags to your insight objects and then use this
      method to register a renderer for the insight widgets that have this tag.

      To learn how to tag insights, see either the GoodData platform or GoodData.CN API documentation.

* To **add additional elements on top of the insight widgets** that are rendered using the built-in renderers, the insight widget customization API provides
   a method to register a "decorator" called `withCustomDecorator()`.

   In some way, the decorators resemble a concept of middleware that you may be familiar with from `express` or `redux`. Instead of
   registering a function that evaluates the widget and insight and returns a React component, for decorators you have to
   register a factory function. This factory function receives a provider for the component to decorate
   and must then return a function to provide a decorator component. The function to return has the same signature as
   the insight provider function that you register in `withCustomProvider`.

   This is best described in code:

     ```javascript
     withCustomDecorator((next) => {
         return (insight, widget) => {
             if (some_condition_to_prevent_decoration) {
                 return undefined;
             }

             function MyCustomDecorator(props) {
                  const Decorated = next(insight, widget);

                  return (
                      <div>
                          <p>My Custom Decoration</p>
                          <Decorated {...props} />
                      </div>
                  )
             }

             return MyCustomDecorator;
         }
     })
     ```

### Customize rendering of KPIs

Call the `kpiWidgets()` method on the customization API to get to the API through which you can customize
how the [KPI widgets](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.idashboardkpicustomizer.html)
on your dashboard will be rendered.

The KPI widgets are a special type of widgets available only on the GoodData platform. Their purpose is to render the value of
a single measure, possibly compared to the value of the measure in the previous period. The KPI widgets additionally
allow dashboard consumers to register alerts for when the measure value changes.

Similar to the insight widget customization API, you can register your own components to render the KPI widgets or
to add extra elements on top of the widgets.

The KPI customization API contains the `withCustomProvider()` and `withCustomDecorator()` methods that behave the same way
the [insight widget methods](18_dashboard_plugins_api.md#customize-rendering-of-insights) do.

### Custom widgets

With custom widgets, you can enrich dashboards with arbitrary content. For example, your dashboard can use
a built-in insight and widget renderers and then use the custom widgets to add extra content such as images,
additional text, or forms.

Your must first register a custom widget type, which is declaration of the custom widget type. The declaration
links the custom widget type name and the React component to use for rendering.

Once you have registered the custom widget type, you can add any number of widgets of this type to your dashboard using the
layout manipulation API.

**NOTE:** The default plugin bootstrapped by the Plugin Development Toolkit defines a sample custom widget
that you can try out right away.

#### Register types of custom widget

Call the `customWidgets()` method on the customization API to get to the API through which you can
register [custom widget types](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.idashboardwidgetcustomizer.html).

Use the `addCustomWidget()` method to register a custom widget type and provide your custom React component. The
props for this component will be of the `IDashboardWidgetProps` type. The `widget` property contains a payload
for the custom widget.

#### Add custom widgets to a dashboard

Once you have a custom widget type registered, you can add instances of the widgets of this type to a dashboard. To do so,
use the layout manipulation API.

Call the `layout()` method on the customization API to get to the API through which you can
[customize the layout](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.idashboardlayoutcustomizer.html).
To be prepared for different types of layouts that may be implemented in the future, the layout manipulation API contains
methods to customize the layout of a particular type.

The Dashboard component supports only the fluid 12-column grid layout. To customize this layout, use
the `customizeFluidLayout()` function to register your customization function.

You have to implement the fluid layout customization function yourself. This function is called with
the following parameters:

-  The layout as currently defined on the dashboard

   The layout is provided so that your code can inspect the layout and determine what and where
   to customize if needed. Depending on your use case, you may or may not need to inspect the layout. For instance, if you
   are adding a custom header on top of the dashboard, you can just do that.

-  [The fluid layout customization API](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.ifluidlayoutcustomizer.html)

   This API allows you to add new sections (rows) to a dashboard or add new items (columns) to existing
   sections.

**NOTE:** The function that you register at this point will be called once during the dashboard initialization to modify
the layout before it is stored in the Dashboard component's state. This API does not support further modifications of the layout that is
already rendered. The Dashboard component provides alpha-level APIs that can be used to add, move, or remove widgets once the Dashboard component is rendered.

#### Create sections, items, and custom widgets

We do not recommend that you create sections, items, and custom widget objects manually. The Dashboard component
contains convenient factory functions to create these objects:

-  `newDashboardSection` creates a new section (row) with a number of items (columns) that contain custom widgets.
-  `newDashboardItem` creates a new item (column) that contains a custom widget.
-  `newCustomWidget` creates a custom widget.

   When creating a custom widget, you have to specify a unique identifier for this widget, the type of the custom widget,
   and optionally additional data to include in the widget.

   The custom widget renderer that you registered previously will receive all this information so you can customize rendering
   based on the additional data.

## Event handler API

The event handler API is an entry point to the event handling and subscription subsystem of the Dashboard component. 
The Dashboard component is designed to be fully observable via events, and you can register handlers for any of these events.

**NOTE:** Keep in mind that the API maturity of most of the events is `@alpha`. That means that they may change in one of the future
versions of GoodData.UI and break your plugin if you try to upgrade to it.

The [event handler API](https://sdk.gooddata.com/gooddata-ui-apidocs/v8.7.0/docs/sdk-ui-dashboard.idashboardeventing.html) has several methods
that can be divided into the following groups:

-  `addEventHandler()` and `removeEventHandler()`

   These are convenience methods that allow you to add or remove event handlers for a single type of the event or
   for all events. You can specify a particular event type such as `"GDC.DASH/EVT.INITIALIZED"`, and add a function to
   receive the event; or, you can specify an event type as `"*"`, and add a function to receive events of all types.

   For more information, see the API reference for the event handler API.

-  `addCustomEventHandler()` and `removeCustomEventHandler()`

   These methods allow you to customize the event handling setup. For each custom event handler,
   you can specify a function to evaluate whether the event should be dispatched to the handler function.

   For more information, see the API reference for the event handler API.

-  `subscribeToStateChanges()` and `unsubscribeFromStateChanges()`

   In more advanced use cases, your non-React code may need to `select` data from the Dashboard component's state using the
   Model Selector APIs. To do that, your code needs the latest version of the Dashboard component's state. Using
   these methods, you can subscribe to receive the state updates.

   **NOTE:** You need to subscribe to receive the state changes only if you have code outside of React components and outside
   of the event handlers that need to `select` from the state. When in React components, you can use the `useDashboardSelector`
   to `select` from the state.

## Interacting with the Dashboard component APIs

The Dashboard component APIs are described [in this article](18_dashboard_component.md) and in the API reference. This section
describes how code in your plugin can access those APIs.

The way to interact with the Dashboard component's APIs depends on from where you want to do so.

### Using the APIs from React components

Your custom React components are mounted right into the dashboard's React tree from where they can use hooks
to interact with the Redux-based dashboard APIs:

-  The `useDashboardSelector` hook helps interact with the Model Selector API.
-  The `useDashboardDispatch` hook helps dispatch actions from the Model Command API.

**NOTE:** All of the actions in the Model Command API are alpha-level APIs and will most likely change in one of the
future releases of the Dashboard component.

### Using the APIs from outside React components

Your custom non-React code may need to use the Model Selector API or dispatch actions from the Model Command API.
Because that code is completely out of any React tree, you need to access these APIs differently.

-  In the **event handler code**, you receive access points to the Model APIs as arguments. Event handler functions have the following arguments:
   -  `event` to handle
   -  The `dashboardDispatch` function to dispatch actions from the Model Command API
   -  The `stateSelect` function to use for accessing the Model Selector API

   The `dashboardDispatch` and `stateSelect` semantics are the same as with the `useDashboardDispatch` and
   `useDashboardSelector` hooks.

-  In **all other code** that is not connected to the Dashboard component using React or using an event handler, you need to
   use the `subscribeToStateChanges()` method. The callback function that you pass to this method will be triggered
   every time the Dashboard component's state changes. Your function can store this somewhere and
   pass it as input to the selectors.

   >**IMPORTANT!** Never modify the state to which you subscribe. GoodData.UI does not support this action. The only supported way to modify the state is through the Model Command API.
