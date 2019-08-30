import React from 'react';
import QueryParamNode from "./QueryParamNode";
import Node from "./Node";

export default class RequestNode extends React.Component {
    state = {
        name: '',
        addInput: '',
        options: [{name: 'default'}]
    };

    nameChange = (event) => {
        let node = this.getNode();
        node.name = event.target.value;
        this.props.valueChanged(node);
    }


    getNode = () => {
        let node = this.props.node;
        if (!node) {
            node = {
                name: '',
                type: 'queryParam',
                options: [{name:'default'}],
            }
        }

        if (!node.options) {
            node.options = [{name: 'default'}];
        }

        if (!node.name) {
            node.name = '';
        }

        if (!node.type) {
            node.type = 'queryParam';
        }

        console.log('child node', this.props.node);
        return node;
    }


    addInputChange = (event) => {
        this.setState({addInput: event.target.value});
    }


    buildNodeValue = () => {
        this.props.valueChanged({
            type: 'queryParam',
            name: this.state.name,
            options: this.state.options
        });
    }


    optionChanged = (option, childNode) => {
        let node = this.getNode();


        let options = node.options;
        const index = options.findIndex((o) => o.name === option.name);

        if (index !== -1) {
            options[index] = {
                name: option.name,
                value: childNode
            }
        }

        console.log('sub node changed', node);

        this.props.valueChanged(node);



    }

    addOption = () => {
        let node = this.getNode();
        if (this.state.addInput.trim().length > 0 && node.options.findIndex((o) => o.name === this.state.addInput) === -1) {

            node.options.push({name: this.state.addInput});

            node.options = node.options.sort((o1, o2) => {
                if (o1.name === 'default') {
                    return 1;
                } else if (o2.name === 'default') {
                    return -1;
                } else {
                    return o1.name.localeCompare(o2.name);
                }
            });


            this.props.valueChanged(node);
        }
    }

    render() {
        const node = this.getNode();
        return (<div className="RequestNode">
            <div>
                <label>Query param name:</label>
                <input type="text" value={node.name} onChange={this.nameChange}/>
            </div>
            <div>
                <label>Add option:</label>
                <input type="text" value={this.state.addInput} onChange={this.addInputChange}/>
                <button onClick={this.addOption}>Add</button>
            </div>
            {node.options.map(o => <div key={o.name}>
                {o.name}: <Node activities={this.props.activities} nowPlaying={this.props.nowPlaying}
                                node={o.value}
                                valueChanged={(node) => this.optionChanged(o, node)}/>
            </div>)}

        </div>);
    }
}

