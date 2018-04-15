import React from 'react';
import SinglePluginSettings from './SinglePluginSettings.jsx';


export default class PluginSettings extends React.Component {
    constructor(props) {
        super(props);
    }


    render() {
        return (<div className="PluginSettings">
            <h2>Plugin Settings</h2>
            {this.props.plugins.map((p, i) => {
                if(p.settings != undefined && p.settings.length >0) {
                    return (<div key={p.id}>
                        <h3>{p.name}</h3>
                        <p>Can be used as: {p.tags}</p>
                        <SinglePluginSettings settings={p.settings} plugin={p.id} values={p.settingsValues}/>
                    </div>)
                }else{
                    <div></div>
                }
            })}
        </div>);
    }
}