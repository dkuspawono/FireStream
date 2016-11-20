//
//  SpotifyInterface.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import Foundation
import Spotify
import SafariServices

private let _spotifySingleton = SpotifyInterface()
class SpotifyInterface: NSObject, SPTAudioStreamingDelegate {
    private var userId: String?
    private var accessToken: String?
    
    private var authController: SFSafariViewController?
    
    private lazy var auth: SPTAuth = {
        let temp = SPTAuth.defaultInstance()
        temp?.clientID = "c2c2a7e68fad4bcca474b814fb549c07"
        temp?.redirectURL = URL(string: "firestream://callback/")
        temp?.sessionUserDefaultsKey = "current session"
        temp?.requestedScopes = [SPTAuthStreamingScope]
        return temp!
    }()
    
    private lazy var player: SPTAudioStreamingController = {
        let temp = SPTAudioStreamingController.sharedInstance()
        temp?.delegate = self
        return temp!
    }()
    
    class func InitilizeUser(userId: String, accessToken: String) {
        _spotifySingleton.userId = userId
        _spotifySingleton.accessToken = accessToken
    }
    
    class func IsInitilized() -> Bool {
        return _spotifySingleton.userId != nil && _spotifySingleton.accessToken != nil
    }
    
    class func GetAuth() -> SPTAuth {
        return _spotifySingleton.auth
    }
    
    class func GetPlayer() -> SPTAudioStreamingController {
        return _spotifySingleton.player
    }
    
    class func Authenticate() -> SFSafariViewController? {
        if let session =  _spotifySingleton.auth.session {
            if session.isValid() {
                LoginWith(accessToken: session.accessToken)
                return nil
            }
        }
        let url = _spotifySingleton.auth.loginURL
        let authViewController = SFSafariViewController.init(url: url!)
        _spotifySingleton.authController = authViewController
        return authViewController
    }
    
    class func DismissAuthController() {
        _spotifySingleton.authController?.dismiss(animated: true, completion: nil)
        _spotifySingleton.authController = nil
    }
    
    class func LoginWith(accessToken: String) {
        if !_spotifySingleton.player.loggedIn {
            //_spotifySingleton.player.login(withAccessToken: accessToken)
        }
        _spotifySingleton.accessToken = accessToken
        _spotifySingleton.userId = _spotifySingleton.auth.session.canonicalUsername
        print(_spotifySingleton.userId ?? "")
    }
    
    internal class func Search(query: String,
                               type: String,
                               postCommandHandler: ((Data?) -> Void)? = nil) {
        let urlString: String = "https://api.spotify.com/v1/search?q=\(query.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed) ?? "")&type=\(type.addingPercentEncoding(withAllowedCharacters: CharacterSet.urlPathAllowed) ?? "")"
        guard let url = URL(string: urlString) else { return }
        var request: URLRequest = URLRequest(url: url)
        request.httpMethod = "GET"
        let task = URLSession.shared.dataTask(with: request) { (data: Data?, response: URLResponse?, errorIn: Error?) -> Void in
            let httpUrlResponse = response as! HTTPURLResponse!
            if (httpUrlResponse != nil) {
                let statusCode = httpUrlResponse?.statusCode
                if statusCode == 200 || statusCode == 201 {
                    postCommandHandler?(data)
                }
            }
        }
        task.resume()
    }
    
    class func GetUserId() -> String? {
        return _spotifySingleton.userId
    }
}
