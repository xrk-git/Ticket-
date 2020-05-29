import React, {Component} from 'react';
import logo from "../assets/images/nba-logoman-word-white.svg"

class TopBar extends Component {
    render() {
        return (
            <div>
                <div className="App">
                    <header className="App-header">
                        <img src={logo} className="App-logo" alt="logo" />
                    </header>
                </div>
            </div>
        );
    }
}

export default TopBar;