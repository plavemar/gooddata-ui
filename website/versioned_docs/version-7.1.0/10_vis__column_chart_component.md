---
title: Column Chart
sidebar_label: Column Chart
copyright: (C) 2007-2018 GoodData Corporation
id: version-7.1.0-column_chart_component
original_id: column_chart_component
---

Column chart shows data in vertical columns. Column charts can display one or multiple measures side by side, divided by either attribute values or by a single measure stacked by attribute values.

![Column Chart Component](assets/column_chart.png "Column Chart Component")

## Structure

```jsx
import '@gooddata/react-components/styles/css/main.css';
import { ColumnChart } from '@gooddata/react-components';

<ColumnChart
    projectId={<workspace-id>}
    measures={<measures>}
    config={<chart-config>}
    sdk={<sdk>}
    …
/>
```

## Example

```jsx
const totalSales = {
    measure: {
        localIdentifier: 'totalSales',
        definition: {
            measureDefinition: {
                item: {
                    identifier: totalSalesIdentifier
                },
                aggregation: 'sum'
            }
        },
        alias: '$ Total Sales',
        format: '#,##0'
    }
};

const month = {
    visualizationAttribute: {
        displayForm: {
            identifier: monthDateIdentifier
        },
        localIdentifier: 'month'
    }
};

<div style={{ height: 300 }}>
    <ColumnChart
        projectId={workspaceId}
        measures={[totalSales]}
        viewBy={month}
    />
</div>
```

## Properties

| Name | Required? | Type | Description |
| :--- | :--- | :--- | :--- |
| projectId | true | string | The workspace ID |
| measures | true | [Measure[]](50_custom__execution.md#measure) | An array of measure definitions |
| viewBy | false | [Attribute](50_custom__execution.md#attribute) &#124; [Attribute[]](50_custom__execution.md#attribute) | An attribute definition or an array of two attribute definitions. If set to a two-attribute array, the first attribute wraps up the second one. |
| stackBy | false | [Attribute](50_custom__execution.md#attribute) | An attribute definition. Do not use `stackBy` in charts with multiple measures. |
| filters | false | [Filter[]](30_tips__filter_visual_components.md) | An array of filter definitions |
| sortBy | false | [SortItem[]](50_custom__result.md#sorting) | An array of sort definitions |
| config | false | [ChartConfig](15_props__chart_config.md) | The chart configuration object |
| locale | false | string | The localization of the chart. Defaults to `en-US`. For other languages, see the [full list of available localizations](https://github.com/gooddata/gooddata-react-components/tree/master/src/translations). |
| drillableItems | false | [DrillableItem[]](15_props__drillable_item.md) | An array of points and attribute values to be drillable. |
| sdk | false | SDK | A configuration object where you can define a custom domain and other API options |
| ErrorComponent | false | Component | A component to be rendered if this component is in error state. See [ErrorComponent](15_props__error_component.md).|
| LoadingComponent | false | Component | A component to be rendered if this component is in loading state. See [LoadingComponent](15_props__loading_component.md).|
| onError | false | Function | A callback when the component updates its error state |
| onExportReady | false | Function | A callback when the component is ready for exporting its data |
| onLoadingChanged | false | Function | A callback when the component updates its loading state |


<!-- These internals are intentionally undocumented
| afterRender | false | Function | A callback after component is rendered |
| dataSource | false | DataSource class | A class that is used to resolve AFM |
| environment | false | string | An Internal property that changes behaviour in Analytical Designer and KPI Dashboards |
| height | false | number | Height of the component in pixels |
| pushData | false | Function | A callback after AFM is resolved |
-->

The following example shows the supported `config` structure with sample values. To see descriptions of individual options, see [ChartConfig section](15_props__chart_config.md).
```javascript
{
    colors: ['rgb(195, 49, 73)', 'rgb(168, 194, 86)'],
    colorPalette: [{
        guid: '01',
        fill: {
            r: 195,
            g: 49,
            b: 73
        }
    }, {
        guid: '02',
        fill: {
            r: 168,
            g: 194,
            b: 86
        }
    }],
    colorMapping: [{
        predicate: (headerItem) => {
            return headerItem.measureHeaderItem && (headerItem.measureHeaderItem.localIdentifier === 'm1_localIdentifier')
        },
        color: {
            type: 'guid',
            value: '02'
        }
    }],
    xaxis: {
        visible: true,
        labelsEnabled: true,
        rotation: 'auto'
    },
    yaxis: {
        visible: true,
        labelsEnabled: true,
        rotation: 'auto',
        min: '20',
        max: '30'
    },
    legend: {
        enabled: true,
        position: 'bottom',
    },
    dataLabels: {
        visible: 'auto'
    },
    grid: {
        enabled: true
    }
    separators: {
        thousand: ',',
        decimal: '.'
    }
}
```
