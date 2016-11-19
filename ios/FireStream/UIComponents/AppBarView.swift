//
//  AppBarView.swift
//  FireStream
//
//  Created by Caleb Adcock on 11/19/16.
//  Copyright © 2016 Syntonic LLC. All rights reserved.
//

import UIKit

class AppBarView: UIView {
    @IBOutlet weak var lblTitle: UILabel!
    @IBOutlet weak var lblDetail: UILabel!
    @IBOutlet weak var btnBack: UIButton!
    @IBOutlet weak var btnRight: UIButton!
    @IBOutlet weak var constraintBtnBack: NSLayoutConstraint!
    
    var showBtnBack: Bool = false {
        didSet {
            constraintBtnBack.constant = showBtnBack ? 48 : 0
            btnBack.isHidden = !showBtnBack
        }
    }
    
    var title: String? {
        didSet {
            lblTitle.text = title
        }
    }
    
    var detailText: String? {
        didSet {
            lblDetail.text = detailText
        }
    }
}
