//
//  ControllerInterface.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit

public enum SegueType: String {
    case Show = "show"
    case Modal = "modal"
    case Root = "root"
}

public enum SegueCommand {
    case ToCreateParty
    case ToPartySearch
    case ToParty
    
    
    var identifier: String {
        switch self {
        case .ToCreateParty: return "CreatePartyViewController"
        case .ToPartySearch: return "PartySearchViewController"
        case .ToParty: return "PartyViewController"
        }
    }
    
    var storyboard: UIStoryboard {
        switch self {
        default: return UIStoryboard(name: "Main", bundle: Bundle.main)
        }
    }
}

class ControllerInterface {
    internal class func DoSegue(segueCommand: SegueCommand, viewController: UIViewController, segueType: SegueType = .Show, extraDataObject: Any? = nil) {
        DispatchQueue.main.async {
            let storyboard = segueCommand.storyboard
            let segueToController = storyboard.instantiateViewController(withIdentifier: segueCommand.identifier)
            
            if let myGizmoController = segueToController as? MaterialViewController {
                myGizmoController.extraDataObject = extraDataObject
            }
            
            switch segueType {
            case .Show:
                viewController.navigationController?.pushViewController(segueToController, animated: true)
                break
            case .Modal:
                viewController.present(segueToController, animated: true, completion: nil)
                break
            case .Root:
                viewController.navigationController?.setViewControllers([segueToController], animated: true)
            }
        }
    }
}
