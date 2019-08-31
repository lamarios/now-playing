import React from 'react';
import SinglePluginSettings from './SinglePluginSettings.jsx';


export default class PluginSettings extends React.Component {
    constructor(props) {
        super(props);
    }


    render() {
        return (<div className="PluginSettings">
            <h2>Plugin Settings</h2>
            <p>You can set up all your plugins here</p>
            {this.props.plugins.map((p, i) => {
                if (p.settings != undefined && p.settings.length > 0) {
                    return (<div key={p.id} className='settings'>
                        <h3>{p.name}</h3>
                        <p>Can be used as: {p.tags && p.tags.map(t => <span className={"tag "+t} key={t}>{t}</span>)}</p>
                        <SinglePluginSettings settings={p.settings} plugin={p.id} values={p.settingsValues}/>
                        {p.loginHtml !== undefined && <div className="extnal-login">
                            <h4>External login link</h4>
                            <div dangerouslySetInnerHTML={{__html: p.loginHtml}}></div>
                        </div>}
                    </div>)
                } else {
                    <div></div>
                }
            })}
        </div>);
    }
}