//
//  Party.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import Foundation

class Party {
    var id: String = ""
    var name: String = ""
    var queue: [Song] = [Song]()
    var requests: [Song] = [Song]()
    var hasPassword: Bool = false
    var hostToken: String = ""
    var hostName: String = ""
    var attendees: Int = 0
    var progress: Int64 = 0
    
    var isHost: Bool {
        return SpotifyInterface.GetUserId() == hostToken
    }
    
    var dictionaryValue:[String:Any] {
        let timestamp = Int64((Date().timeIntervalSince1970 * 1000))
        return [
            "id": id,
            "name": name,
            "queue": queue.map {$0.dictionaryValue},
            "requests": requests.map {$0.dictionaryValue},
            "hasPassword": hasPassword,
            "hostToken": hostToken,
            "hostName": hostName,
            "attendees": attendees,
            "progress": progress,
            "timestamp": timestamp
        ]
    }
    
    init(dict: [String: Any]) {
        if let val = dict["id"] as? String {
            self.id = val
        }
        if let val = dict["name"] as? String {
            self.name = val
        }
        if let val = dict["queue"] as? [[String:Any]] {
            self.queue = val.map { Song(dict: $0) }
        }
        if let val = dict["requests"] as? [[String:Any]] {
            self.requests = val.map { Song(dict: $0) }
        }
        if let val = dict["hasPassword"] as? Bool {
            self.hasPassword = val
        }
        if let val = dict["hostToken"] as? String {
            self.hostToken = val
        }
        if let val = dict["hostName"] as? String {
            self.hostName = val
        }
        if let val = dict["attendees"] as? Int {
            attendees = val
        }
        if let val = dict["progress"] as? Int64 {
            progress = val
        }
    }
}
