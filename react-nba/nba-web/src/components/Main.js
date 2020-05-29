import React, {Component} from 'react';
import Profile from "./Profile"
import nba from 'nba';
import DataViewContainer from "./DataViewContainer";
import SearchBar from "./SearchBar";
import { DEFAULT_PLAYER_INFO } from '../constants';


class Main extends Component {
    // state = {
    //     playerId: nba.findPlayer('Stephen Curry').playerId,
    //     playerInfo: {},
    // }
    state = {
        playerInfo: DEFAULT_PLAYER_INFO,
    }

    // componentDidMount() {
    //     nba.stats.playerInfo({PlayerID: this.state.playerId
    //     }).then((info) => {
    //         const playerInfo = Object.assign(info.commonPlayerInfo[0],
    //             info.playerHeadlineStats[0]);
    //         console.log('playerInfo', playerInfo);
    //         this.setState({ playerInfo });
    //     });
    // }

    componentDidMount() {
        this.loadPlayerInfo(this.state.playerInfo.fullName);
    }

    loadPlayerInfo = (playerName) => {
        nba.stats.playerInfo({ PlayerID: nba.findPlayer(playerName).playerId }).then((info) => {
            const playerInfo = Object.assign(info.commonPlayerInfo[0], info.playerHeadlineStats[0]);
            this.setState({ playerInfo });
        });
    }

    handleSelectPlayer = (playerName) => {
        this.loadPlayerInfo(playerName);
    }

    render() {
        return (
            <div className="main">
                <SearchBar loadPlayerInfo={this.handleSelectPlayer}/>
                <div className="player">
                    <Profile playerInfo={this.state.playerInfo}/>
                    <DataViewContainer playerId={this.state.playerInfo.playerId}/>
                </div>
            </div>
        );
    }
}

export default Main;