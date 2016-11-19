//
//  MaterialController.swift
//  Life
//
//  Created by Caleb Adcock on 10/12/16.
//  Copyright © 2016 Caleb Adcock. All rights reserved.
//

import UIKit
import Material

class MaterialNavigationController: UINavigationController {
    
    lazy var defaultAppBarView: AppBarView = {
        let temp = Bundle.main.loadNibNamed(
            "AppBarView",
            owner: nil,
            options: nil)![0] as! AppBarView
        temp.translatesAutoresizingMaskIntoConstraints = false
        return temp
    }()
    
    lazy var appBar: UIView = {
        // Set shadow
        let temp = UIView()
        temp.layer.shadowColor = UIColor.black.cgColor
        temp.layer.shadowOpacity = 0.5
        temp.layer.shadowOffset = CGSize.zero
        temp.layer.shadowRadius = 0
        
        // Set status bar
        let topView = UIView()
        topView.translatesAutoresizingMaskIntoConstraints = false
        topView.backgroundColor = .colorPrimaryDark
        temp.addSubview(topView)
        temp.backgroundColor = .colorPrimary
        temp.translatesAutoresizingMaskIntoConstraints = false
        temp.addConstraints([
            NSLayoutConstraint(item: topView, attribute: .leading, relatedBy: .equal, toItem: temp, attribute: .leading, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: topView, attribute: .trailing, relatedBy: .equal, toItem: temp, attribute: .trailing, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: topView, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: 1, constant: 24),
            NSLayoutConstraint(item: topView, attribute: .top, relatedBy: .equal, toItem: temp, attribute: .top, multiplier: 1, constant: 0)
        ])
        
        // Set default view
        temp.addSubview(self.defaultAppBarView)
        temp.addConstraints([
            NSLayoutConstraint(item: temp, attribute: .leading, relatedBy: .equal, toItem: self.defaultAppBarView, attribute: .leading, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: temp, attribute: .trailing, relatedBy: .equal, toItem: self.defaultAppBarView, attribute: .trailing, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: temp, attribute: .bottom, relatedBy: .equal, toItem: self.defaultAppBarView, attribute: .bottom, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: topView, attribute: .bottom, relatedBy: .equal, toItem: self.defaultAppBarView, attribute: .top, multiplier: 1, constant: 0)
            ])
        return temp
    }()
    
    var doShowAppBar: Bool = false {
        didSet {
            if doShowAppBar {
                self.appBar.isHidden = !doShowAppBar
            }
            UIView.animate(withDuration: 0.2, animations: {
                self.appBar.frame.origin.y = self.doShowAppBar ? 0 : -80
                }, completion: { (finished) in
                    if finished {
                        self.appBar.isHidden = !self.doShowAppBar
                    }
            })
        }
    }
    
    var doShowShaddow: Bool = false {
        didSet {
            self.appBar.layer.shadowRadius = doShowShaddow ? 4 : 0
        }
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        self.isNavigationBarHidden = true
        appBar.isHidden = true
        self.view.addSubview(appBar)
        self.view.addConstraints([
            NSLayoutConstraint(item: appBar, attribute: .leading, relatedBy: .equal, toItem: view, attribute: .leading, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: appBar, attribute: .trailing, relatedBy: .equal, toItem: view, attribute: .trailing, multiplier: 1, constant: 0),
            NSLayoutConstraint(item: appBar, attribute: .height, relatedBy: .equal, toItem: nil, attribute: .notAnAttribute, multiplier: 1, constant: 80),
            NSLayoutConstraint(item: appBar, attribute: .top, relatedBy: .equal, toItem: view, attribute: .top, multiplier: 1, constant: 0)
        ])
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.p
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }
    
    override func pushViewController(_ viewController: UIViewController, animated: Bool) {
        super.pushViewController(viewController, animated: animated)
        updateAppBar()
    }
    
    func backAction() {
        popViewController(animated: true)
        updateAppBar()
    }
    
    func updateAppBar() {
        defaultAppBarView.title = viewControllers.last?.title
        defaultAppBarView.showBtnBack = viewControllers.count > 1
        if let materialController = viewControllers.last as? MaterialViewController {
            doShowShaddow = materialController.showAppBarShadow
            doShowAppBar = materialController.showAppBar
            defaultAppBarView.detailText = materialController.detailText
        } else {
            doShowShaddow = false
            doShowAppBar = false
        }
    }
}
