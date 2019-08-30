import React from 'react';
import Node from "./Node";

export default class QueryParamNode extends React.Component {
    state = {
        name: '',
        addInput: '',
        options: [{name: 'default'}]
    };

    nameChange = (event) => {
        this.setState({name: event.target.value}, this.buildNodeValue);
    }

    addInputChange = (event) => {
        this.setState({addInput: event.target.value}, this.buildNodeValue);
    }


    buildNodeValue = () => {
        this.props.valueChanged({
            type: 'queryParam',
            name: this.state.name,
            options: this.state.options
        });
    }


    optionChanged = (option, childNode) => {
        console.log('child node', childNode)
        let options = this.state.options;
        const index = options.findIndex((o) => o.name === option.name);

        if (index !== -1) {
            options[index] = {
                name: option.name,
                value: childNode
            }
        }

        this.setState({options: options}, this.buildNodeValue)

    }

    addOption = () => {
        if (this.state.addInput.trim().length > 0 && this.state.options.findIndex((o) => o.name === this.state.addInput) === -1) {
            let options = this.state.options;
            options.push({name: this.state.addInput});

            options = options.sort((o1, o2) => {
                if (o1.name === 'default') {
                    return 1;
                } else if (o2.name === 'default') {
                    return -1;
                } else {
                    return o1.name.localeCompare(o2.name);
                }
            });

            this.setState({options: options});
        }
    }

    render() {
        const options = this.state.options;
        return (<div className="QueryParamNode">
            <div>
                <label>Query param name:</label>
                <input type="text" value={this.state.name} onChange={this.nameChange}/>
            </div>
            <div>
                <label>Add option:</label>
                <input type="text" value={this.state.addInput} onChange={this.addInputChange}/>
                <button onClick={this.addOption}>Add</button>
            </div>
            {options && options.map(o => <div key={o.name}>
                {o.name}: <Node activities={this.props.activities} nowPlaying={this.props.nowPlaying}
                                valueChanged={(node) => this.optionChanged(o, node)}/>
            </div>)}

        </div>);
    }
}